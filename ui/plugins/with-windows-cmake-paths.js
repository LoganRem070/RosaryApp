const {
  withAppBuildGradle,
  withProjectBuildGradle,
} = require("@expo/config-plugins");

const objectPathMax = 250;
const cmakeArgument = `-DCMAKE_OBJECT_PATH_MAX=${objectPathMax}`;

function addCmakeArgument(contents, anchor) {
  if (contents.includes(cmakeArgument)) {
    return contents;
  }

  if (!contents.includes(anchor)) {
    throw new Error(`Could not find Gradle insertion point: ${anchor}`);
  }

  return contents.replace(
    anchor,
    `        externalNativeBuild {
            cmake {
                arguments "${cmakeArgument}"
            }
        }

${anchor}`,
  );
}

function withAppCmakePaths(config) {
  return withAppBuildGradle(config, (mod) => {
    mod.modResults.contents = addCmakeArgument(
      mod.modResults.contents,
      '        buildConfigField "String", "REACT_NATIVE_RELEASE_LEVEL"',
    );
    return mod;
  });
}

function withReanimatedCmakePaths(config) {
  return withProjectBuildGradle(config, (mod) => {
    const marker = "// Keep Reanimated's native object paths below Windows MAX_PATH.";
    if (mod.modResults.contents.includes(marker)) {
      return mod;
    }

    const anchor = 'apply plugin: "com.facebook.react.rootproject"';
    if (!mod.modResults.contents.includes(anchor)) {
      throw new Error(`Could not find Gradle insertion point: ${anchor}`);
    }

    mod.modResults.contents = mod.modResults.contents.replace(
      anchor,
      `${anchor}

${marker}
subprojects { subproject ->
    if (subproject.name == "react-native-reanimated") {
        subproject.afterEvaluate {
            def cmakeArguments = subproject.extensions.getByName("android").defaultConfig.externalNativeBuild.cmake.arguments
            cmakeArguments.removeAll { it.startsWith("-DCMAKE_OBJECT_PATH_MAX=") }
            cmakeArguments.add("${cmakeArgument}")
        }
    }
}`,
    );
    return mod;
  });
}

module.exports = (config) => {
  if (process.platform !== "win32") {
    return config;
  }

  return withReanimatedCmakePaths(withAppCmakePaths(config));
};
