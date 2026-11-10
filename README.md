# RosaryApp

## Credits:
- Logan Rembisz
- Ryan Ackerley

AI was used for debugging stuff

## build api
cd api/my-java-api
mvn compile exec:java

## test ui
cd ui
npm run web OR npm run android

## build ui
cd ui/android
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk