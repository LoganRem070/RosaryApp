package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class sql_testing {

  private static final String CREATE_PRAYERS = """
      CREATE TABLE IF NOT EXISTS PRAYERS (
          PRAYER_ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
          NAME VARCHAR(255) NOT NULL,
          LANGUAGE VARCHAR(255) NOT NULL,
          TEXT TEXT NOT NULL,
          UNIQUE (NAME, LANGUAGE)
      )
      """;

  private static final String INSERT_PRAYERS = """
      INSERT INTO PRAYERS (NAME, LANGUAGE, TEXT) VALUES
      ('Sign of the Cross', 'English', 'In the name of the Father, and of the Son, and of the Holy Spirit. Amen.'),
      ('Apostles'' Creed', 'English', 'I believe in God, the Father almighty, Creator of heaven and earth; and in Jesus Christ, his only Son, our Lord, who was conceived by the Holy Spirit, born of the Virgin Mary, suffered under Pontius Pilate, was crucified, died, and was buried. He descended into hell; on the third day he rose again from the dead; he ascended into heaven, and is seated at the right hand of God the Father almighty; from there he will come to judge the living and the dead. I believe in the Holy Spirit, the holy Catholic Church, the communion of saints, the forgiveness of sins, the resurrection of the body, and life everlasting. Amen.'),
      ('Our Father', 'English', 'Our Father, who art in heaven, hallowed be thy name; thy kingdom come, thy will be done on earth as it is in heaven. Give us this day our daily bread, and forgive us our trespasses as we forgive those who trespass against us; and lead us not into temptation, but deliver us from evil. Amen.'),
      ('Hail Mary', 'English', 'Hail Mary, full of grace, the Lord is with thee. Blessed art thou among women, and blessed is the fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for us sinners, now and at the hour of our death. Amen.'),
      ('Glory Be', 'English', 'Glory be to the Father, and to the Son, and to the Holy Spirit. As it was in the beginning, is now, and ever shall be, world without end. Amen.'),
      ('Fatima Prayer', 'English', 'O my Jesus, forgive us our sins, save us from the fires of hell, and lead all souls to Heaven, especially those most in need of thy mercy.'),
      ('Hail Holy Queen', 'English', 'Hail, Holy Queen, Mother of mercy, our life, our sweetness and our hope. To thee do we cry, poor banished children of Eve; to thee do we send up our sighs, mourning and weeping in this valley of tears. Turn then, most gracious advocate, thine eyes of mercy toward us, and after this our exile, show unto us the blessed fruit of thy womb, Jesus. O clement, O loving, O sweet Virgin Mary. Pray for us, O Holy Mother of God, that we may be made worthy of the promises of Christ. Amen.'),
      ('Closing Prayer', 'English', 'O God, whose only begotten Son, by his life, death, and resurrection, has purchased for us the rewards of eternal life, grant, we beseech thee, that meditating upon these mysteries of the Most Holy Rosary of the Blessed Virgin Mary, we may imitate what they contain and obtain what they promise, through the same Christ our Lord. Amen.'),
      ('St. Michael''s Prayer', 'English', 'St. Michael the Archangel, defend us in battle. Be our protection against the wickedness and snares of the devil. May God rebuke him, we humbly pray, and do thou, O Prince of the heavenly host, by the power of God, cast into hell Satan and all the evil spirits who prowl about the world seeking the ruin of souls. Amen.')
      ON CONFLICT (NAME, LANGUAGE) DO NOTHING
      """;

    private static final String CREATE_ROSARY = """
        CREATE TABLE IF NOT EXISTS ROSARY (
        MYSTERY VARCHAR(255) NOT NULL,
        DECADE INT NOT NULL,
        NUMBER INT NOT NULL,
        READING VARCHAR(255),
        PRAYER_ID INT NOT NULL,
        IMAGE BYTEA,
        PRIMARY KEY (MYSTERY, DECADE, NUMBER),
        FOREIGN KEY (PRAYER_ID) REFERENCES PRAYERS(PRAYER_ID)
        );
        """;

    private static final String INSERT_ROSARIES = """
        INSERT INTO ROSARY (MYSTERY, DECADE, NUMBER, READING, PRAYER_ID, IMAGE) VALUES
        ('Joyful', 0, 0, '', 1, NULL),
        ('Joyful', 0, 1, NULL, 2, NULL),
        ('Joyful', 0, 2, NULL, 3, NULL),
        ('Joyful', 0, 3, NULL, 4, NULL),
        ('Joyful', 0, 4, NULL, 4, NULL),
        ('Joyful', 0, 5, NULL, 4, NULL),
        ('Joyful', 0, 6, NULL, 5, NULL),
        ('Joyful', 0, 7, NULL, 6, NULL),
        ('Joyful', 1, 0, '1. The Annunciation\nFruit: Humility', 3, NULL),
        ('Joyful', 1, 1, NULL, 4, NULL),
        ('Joyful', 1, 2, NULL, 4, NULL),
        ('Joyful', 1, 3, NULL, 4, NULL),
        ('Joyful', 1, 4, NULL, 4, NULL),
        ('Joyful', 1, 5, NULL, 4, NULL),
        ('Joyful', 1, 6, NULL, 4, NULL),
        ('Joyful', 1, 7, NULL, 4, NULL),
        ('Joyful', 1, 8, NULL, 4, NULL),
        ('Joyful', 1, 9, NULL, 4, NULL),
        ('Joyful', 1, 10, NULL, 4, NULL),
        ('Joyful', 1, 11, NULL, 5, NULL),
        ('Joyful', 1, 12, NULL, 6, NULL),
        ('Joyful', 2, 0, '2. The Visitation\nFruit: Charity', 3, NULL),
        ('Joyful', 2, 1, NULL, 4, NULL),
        ('Joyful', 2, 2, NULL, 4, NULL),
        ('Joyful', 2, 3, NULL, 4, NULL),
        ('Joyful', 2, 4, NULL, 4, NULL),
        ('Joyful', 2, 5, NULL, 4, NULL),
        ('Joyful', 2, 6, NULL, 4, NULL),
        ('Joyful', 2, 7, NULL, 4, NULL),
        ('Joyful', 2, 8, NULL, 4, NULL),
        ('Joyful', 2, 9, NULL, 4, NULL),
        ('Joyful', 2, 10, NULL, 4, NULL),
        ('Joyful', 2, 11, NULL, 5, NULL),
        ('Joyful', 2, 12, NULL, 6, NULL),
        ('Joyful', 3, 0, '3. The Nativity\nFruit: Poverty', 3, NULL),
        ('Joyful', 3, 1, NULL, 4, NULL),
        ('Joyful', 3, 2, NULL, 4, NULL),
        ('Joyful', 3, 3, NULL, 4, NULL),
        ('Joyful', 3, 4, NULL, 4, NULL),
        ('Joyful', 3, 5, NULL, 4, NULL),
        ('Joyful', 3, 6, NULL, 4, NULL),
        ('Joyful', 3, 7, NULL, 4, NULL),
        ('Joyful', 3, 8, NULL, 4, NULL),
        ('Joyful', 3, 9, NULL, 4, NULL),
        ('Joyful', 3, 10, NULL, 4, NULL),
        ('Joyful', 3, 11, NULL, 5, NULL),
        ('Joyful', 3, 12, NULL, 6, NULL),
        ('Joyful', 4, 0, '4. The Presentation\nFruit: Obedience', 3, NULL),
        ('Joyful', 4, 1, NULL, 4, NULL),
        ('Joyful', 4, 2, NULL, 4, NULL),
        ('Joyful', 4, 3, NULL, 4, NULL),
        ('Joyful', 4, 4, NULL, 4, NULL),
        ('Joyful', 4, 5, NULL, 4, NULL),
        ('Joyful', 4, 6, NULL, 4, NULL),
        ('Joyful', 4, 7, NULL, 4, NULL),
        ('Joyful', 4, 8, NULL, 4, NULL),
        ('Joyful', 4, 9, NULL, 4, NULL),
        ('Joyful', 4, 10, NULL, 4, NULL),
        ('Joyful', 4, 11, NULL, 5, NULL),
        ('Joyful', 4, 12, NULL, 6, NULL),
        ('Joyful', 5, 0, '5. The Finding of Jesus in the Temple\nFruit: Wisdom', 3, NULL),
        ('Joyful', 5, 1, NULL, 4, NULL),
        ('Joyful', 5, 2, NULL, 4, NULL),
        ('Joyful', 5, 3, NULL, 4, NULL),
        ('Joyful', 5, 4, NULL, 4, NULL),
        ('Joyful', 5, 5, NULL, 4, NULL),
        ('Joyful', 5, 6, NULL, 4, NULL),
        ('Joyful', 5, 7, NULL, 4, NULL),
        ('Joyful', 5, 8, NULL, 4, NULL),
        ('Joyful', 5, 9, NULL, 4, NULL),
        ('Joyful', 5, 10, NULL, 4, NULL),
        ('Joyful', 5, 11, NULL, 5, NULL),
        ('Joyful', 5, 12, NULL, 6, NULL),
        ('Joyful', 6, 0, NULL, 7, NULL),
        ('Joyful', 6, 1, NULL, 8, NULL),
        ('Joyful', 6, 2, NULL, 9, NULL),
        ('Joyful', 6, 3, NULL, 0, NULL),
        ('Sorrowful', 1, 0, '1. The Agony in the Garden\nFruit: Contrition', 3, NULL),
        ('Sorrowful', 1, 1, NULL, 4, NULL),
        ('Sorrowful', 1, 2, NULL, 4, NULL),
        ('Sorrowful', 1, 3, NULL, 4, NULL),
        ('Sorrowful', 1, 4, NULL, 4, NULL),
        ('Sorrowful', 1, 5, NULL, 4, NULL),
        ('Sorrowful', 1, 6, NULL, 4, NULL),
        ('Sorrowful', 1, 7, NULL, 4, NULL),
        ('Sorrowful', 1, 8, NULL, 4, NULL),
        ('Sorrowful', 1, 9, NULL, 4, NULL),
        ('Sorrowful', 1, 10, NULL, 4, NULL),
        ('Sorrowful', 1, 11, NULL, 5, NULL),
        ('Sorrowful', 1, 12, NULL, 6, NULL),
        ('Sorrowful', 2, 0, '2. The Scourging at the Pillar\nFruit: Purity', 3, NULL),
        ('Sorrowful', 2, 1, NULL, 4, NULL),
        ('Sorrowful', 2, 2, NULL, 4, NULL),
        ('Sorrowful', 2, 3, NULL, 4, NULL),
        ('Sorrowful', 2, 4, NULL, 4, NULL),
        ('Sorrowful', 2, 5, NULL, 4, NULL),
        ('Sorrowful', 2, 6, NULL, 4, NULL),
        ('Sorrowful', 2, 7, NULL, 4, NULL),
        ('Sorrowful', 2, 8, NULL, 4, NULL),
        ('Sorrowful', 2, 9, NULL, 4, NULL),
        ('Sorrowful', 2, 10, NULL, 4, NULL),
        ('Sorrowful', 2, 11, NULL, 5, NULL),
        ('Sorrowful', 2, 12, NULL, 6, NULL),
        ('Sorrowful', 3, 0, '3. The Crowning with Thorns\nFruit: Humility', 3, NULL),
        ('Sorrowful', 3, 1, NULL, 4, NULL),
        ('Sorrowful', 3, 2, NULL, 4, NULL),
        ('Sorrowful', 3, 3, NULL, 4, NULL),
        ('Sorrowful', 3, 4, NULL, 4, NULL),
        ('Sorrowful', 3, 5, NULL, 4, NULL),
        ('Sorrowful', 3, 6, NULL, 4, NULL),
        ('Sorrowful', 3, 7, NULL, 4, NULL),
        ('Sorrowful', 3, 8, NULL, 4, NULL),
        ('Sorrowful', 3, 9, NULL, 4, NULL),
        ('Sorrowful', 3, 10, NULL, 4, NULL),
        ('Sorrowful', 3, 11, NULL, 5, NULL),
        ('Sorrowful', 3, 12, NULL, 6, NULL),
        ('Sorrowful', 4, 0, '4. The Carrying of the Cross\nFruit: Patience', 3, NULL),
        ('Sorrowful', 4, 1, NULL, 4, NULL),
        ('Sorrowful', 4, 2, NULL, 4, NULL),
        ('Sorrowful', 4, 3, NULL, 4, NULL),
        ('Sorrowful', 4, 4, NULL, 4, NULL),
        ('Sorrowful', 4, 5, NULL, 4, NULL),
        ('Sorrowful', 4, 6, NULL, 4, NULL),
        ('Sorrowful', 4, 7, NULL, 4, NULL),
        ('Sorrowful', 4, 8, NULL, 4, NULL),
        ('Sorrowful', 4, 9, NULL, 4, NULL),
        ('Sorrowful', 4, 10, NULL, 4, NULL),
        ('Sorrowful', 4, 11, NULL, 5, NULL),
        ('Sorrowful', 4, 12, NULL, 6, NULL),
        ('Sorrowful', 5, 0, '5. The Crucifixion\nFruit: Compassion', 3, NULL),
        ('Sorrowful', 5, 1, NULL, 4, NULL),
        ('Sorrowful', 5, 2, NULL, 4, NULL),
        ('Sorrowful', 5, 3, NULL, 4, NULL),
        ('Sorrowful', 5, 4, NULL, 4, NULL),
        ('Sorrowful', 5, 5, NULL, 4, NULL),
        ('Sorrowful', 5, 6, NULL, 4, NULL),
        ('Sorrowful', 5, 7, NULL, 4, NULL),
        ('Sorrowful', 5, 8, NULL, 4, NULL),
        ('Sorrowful', 5, 9, NULL, 4, NULL),
        ('Sorrowful', 5, 10, NULL, 4, NULL),
        ('Sorrowful', 5, 11, NULL, 5, NULL),
        ('Sorrowful', 5, 12, NULL, 6, NULL),
        ('Sorrowful', 6, 0, NULL, 7, NULL),
        ('Sorrowful', 6, 1, NULL, 8, NULL),
        ('Sorrowful', 6, 2, NULL, 9, NULL),
        ('Sorrowful', 6, 3, NULL, 0, NULL),
        ON CONFLICT (MYSTERY, DECADE, NUMBER) DO NOTHING
        """;

  private sql_testing() {
  }

  /**
   * Creates the PRAYERS table if needed and inserts the default prayers.
   * Prayers that already exist (same NAME and LANGUAGE) are skipped.
   */
  public static void seed(String connUrl) throws SQLException {
    try (Connection conn = DriverManager.getConnection(connUrl);
         Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(CREATE_ROSARY);
      int inserted = stmt.executeUpdate(INSERT_ROSARIES);
      System.out.println(inserted);
    }
  }
}