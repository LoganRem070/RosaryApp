package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class sql_testing {

  private static final String CREATE_PRAYERS = """
      CREATE TABLE IF NOT EXISTS PRAYERS (
            PRAYER_ID INT NOT NULL PRIMARY KEY,
            NAME VARCHAR(255) NOT NULL,
            LANGUAGE VARCHAR(255) NOT NULL,
            TEXT TEXT NOT NULL,
            UNIQUE (NAME, LANGUAGE)
        );
      """;

  private static final String INSERT_PRAYERS = """
      INSERT INTO PRAYERS (PRAYER_ID, NAME, LANGUAGE, TEXT) VALUES
      (1, 'Sign of the Cross', 'English', 'In the name of the Father, and of the Son, and of the Holy Spirit. Amen.'),
      (2, 'Apostles'' Creed', 'English', 'I believe in God, the Father almighty, Creator of heaven and earth; and in Jesus Christ, his only Son, our Lord, who was conceived by the Holy Spirit, born of the Virgin Mary, suffered under Pontius Pilate, was crucified, died, and was buried. He descended into hell; on the third day he rose again from the dead; he ascended into heaven, and is seated at the right hand of God the Father almighty; from there he will come to judge the living and the dead. I believe in the Holy Spirit, the holy Catholic Church, the communion of saints, the forgiveness of sins, the resurrection of the body, and life everlasting. Amen.'),
      (3, 'Our Father', 'English', 'Our Father, who art in heaven, hallowed be thy name; thy kingdom come, thy will be done on earth as it is in heaven. Give us this day our daily bread, and forgive us our trespasses as we forgive those who trespass against us; and lead us not into temptation, but deliver us from evil. Amen.'),
      (4, 'Hail Mary', 'English', 'Hail Mary, full of grace, the Lord is with thee. Blessed art thou among women, and blessed is the fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for us sinners, now and at the hour of our death. Amen.'),
      (5, 'Glory Be', 'English', 'Glory be to the Father, and to the Son, and to the Holy Spirit. As it was in the beginning, is now, and ever shall be, world without end. Amen.'),
      (6, 'Fatima Prayer', 'English', 'O my Jesus, forgive us our sins, save us from the fires of hell, and lead all souls to Heaven, especially those most in need of thy mercy.'),
      (7, 'Hail Holy Queen', 'English', 'Hail, Holy Queen, Mother of mercy, our life, our sweetness and our hope. To thee do we cry, poor banished children of Eve; to thee do we send up our sighs, mourning and weeping in this valley of tears. Turn then, most gracious advocate, thine eyes of mercy toward us, and after this our exile, show unto us the blessed fruit of thy womb, Jesus. O clement, O loving, O sweet Virgin Mary. Pray for us, O Holy Mother of God, that we may be made worthy of the promises of Christ. Amen.'),
      (8, 'Closing Prayer', 'English', 'O God, whose only begotten Son, by his life, death, and resurrection, has purchased for us the rewards of eternal life, grant, we beseech thee, that meditating upon these mysteries of the Most Holy Rosary of the Blessed Virgin Mary, we may imitate what they contain and obtain what they promise, through the same Christ our Lord. Amen.'),
      (9, 'St. Michael''s Prayer', 'English', 'St. Michael the Archangel, defend us in battle. Be our protection against the wickedness and snares of the devil. May God rebuke him, we humbly pray, and do thou, O Prince of the heavenly host, by the power of God, cast into hell Satan and all the evil spirits who prowl about the world seeking the ruin of souls. Amen.')
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
        ('Joyful', 0, 0, NULL, 1, NULL),
        ('Joyful', 0, 1, NULL, 2, NULL),
        ('Joyful', 0, 2, NULL, 3, NULL),
        ('Joyful', 0, 3, 'For Faith', 4, NULL),
        ('Joyful', 0, 4, 'For Hope', 4, NULL),
        ('Joyful', 0, 5, 'For Charity', 4, NULL),
        ('Joyful', 0, 6, NULL, 5, NULL),
        ('Joyful', 0, 7, NULL, 6, NULL),
        ('Joyful', 1, 0, '1. The Annunciation\nFruit: Humility', 3, NULL),
        ('Joyful', 1, 1, 'In the sixth month, the angel Gabriel was sent from God to a virgin, and the virgin''s name was Mary (Luke 1:26, 27)', 4, NULL),
        ('Joyful', 1, 2, 'And the angel came to her and said, "Hail, full of grace, the Lord is with you! Blessed are you among women." (Luke 1:28)', 4, NULL),
        ('Joyful', 1, 3, 'But she was greatly troubled at the saying, and considered in her mind what sort of greeting this might be. (Luke 1:29)', 4, NULL),
        ('Joyful', 1, 4, 'And the angel said to her, "Do not be afraid, Mary, for you have found grace with God." (Luke 1:30)', 4, NULL),
        ('Joyful', 1, 5, 'And behold, you shall conceive in your womb and bear a son, and you shall name him Jesus. (Luke 1:31)', 4, NULL),
        ('Joyful', 1, 6, 'He will be great and will be called the Son of the Most High; and of His Kingdom there will be no end. (Luke 1:32-33)', 4, NULL),
        ('Joyful', 1, 7, 'And Mary said to the angel, "How can this be, since I have no husband?" (Luke 1:34)', 4, NULL),
        ('Joyful', 1, 8, 'And the angel said to her, "The Holy Spirit will come upon you, and the power of the Most High will overshadow you" (Luke 1:35)', 4, NULL),
        ('Joyful', 1, 9, 'Therefore the child to be born will be called Holy, the Son of God. (Luke 1:35)', 4, NULL),
        ('Joyful', 1, 10, 'And Mary said, "Behold, I am the handmaid of the Lord; let it be to me according to your word." And the angel departed from her. (Luke 1:38)', 4, NULL),
        ('Joyful', 1, 11, NULL, 5, NULL),
        ('Joyful', 1, 12, NULL, 6, NULL),
        ('Joyful', 2, 0, '2. The Visitation\nFruit: Love of Neighbor', 3, NULL),
        ('Joyful', 2, 1, 'In those days Mary arose and went with haste into the hill country, to a city of Judah, and she entered the house of Zechariah and greeted Elizabeth. (Luke 1:39-40)', 4, NULL),
        ('Joyful', 2, 2, 'And when Elizabeth heard the greeting of Mary, the babe leaped in her womb; and Elizabeth was filled with the Holy Spirit. (Luke 1:41)', 4, NULL),
        ('Joyful', 2, 3, 'And she exclaimed with a loud cry, "Blessed are you among women, and blessed is the fruit of your womb!" (Luke 1:42)', 4, NULL),
        ('Joyful', 2, 4, 'And blessed is she who believed that there would be a fulfillment of what was spoken to her by the Lord. (Luke 1:45)', 4, NULL),
        ('Joyful', 2, 5, 'And Mary said, "My soul magnifies the Lord, and my spirit rejoices in God my Savior; for He has regarded the low estate of His handmaiden." (Luke 1:46-48)', 4, NULL),
        ('Joyful', 2, 6, 'For behold, henceforth all generations will call me blessed; for He who is mighty has done great things for me (Luke 1:48-49)', 4, NULL),
        ('Joyful', 2, 7, 'And Holy is His name. And His mercy is on those who fear Him from generation to generation. (Luke 1:49-50)', 4, NULL),
        ('Joyful', 2, 8, 'He has shown strength with His arm; He has scattered the proud in the imagination of their hearts. (Luke 1:51)', 4, NULL),
        ('Joyful', 2, 9, 'He has put down the mighty from their thrones, and exalted those of low degree. (Luke 1:52)', 4, NULL),
        ('Joyful', 2, 10, 'He has filled the hungry with good things, and the rich He has sent empty away. (Luke 1:53)', 4, NULL),
        ('Joyful', 2, 11, NULL, 5, NULL),
        ('Joyful', 2, 12, NULL, 6, NULL),
        ('Joyful', 3, 0, '3. The Nativity\nFruit: Detachment', 3, NULL),
        ('Joyful', 3, 1, 'And while they were there, the time came for her to be delivered. (Luke 2:6)', 4, NULL),
        ('Joyful', 3, 2, 'And she gave birth to her first-born son and wrapped him in swaddling clothes, and laid him in a manger, because there was no place for them in the inn. (Luke 2:7)', 4, NULL),
        ('Joyful', 3, 3, 'And in the same region there were shepherds out in the field, keeping watch over their flock by night. (Luke 2:8)', 4, NULL),
        ('Joyful', 3, 4, 'And an angel of the Lord appeared to them, and the glory of the Lord shone around them, and they were filled with fear. (Luke 2:9)', 4, NULL),
        ('Joyful', 3, 5, 'Be not afraid; for behold, I bring you good news of a great joy which will come to all the people. (Luke 2:10)', 4, NULL),
        ('Joyful', 3, 6, 'For to you is born this day in the city of David a Savior, who is Christ the Lord. (Luke 2:11)', 4, NULL),
        ('Joyful', 3, 7, 'Glory to God in the highest, and on earth peace among men with whom He is pleased. (Luke 2:14)', 4, NULL),
        ('Joyful', 3, 8, 'And behold, Magi came from the East, and entering they found the child with Mary, His mother (Matthew 2:11)', 4, NULL),
        ('Joyful', 3, 9, 'And falling down they worshipped Him and they offered Him gifts of gold, frankincense, and myrrh. (Matthew 2:11)', 4, NULL),
        ('Joyful', 3, 10, 'Mary kept all these things, pondering them in her heart. (Luke 2:19)', 4, NULL),
        ('Joyful', 3, 11, NULL, 5, NULL),
        ('Joyful', 3, 12, NULL, 6, NULL),
        ('Joyful', 4, 0, '4. The Presentation\nFruit: Obedience', 3, NULL),
        ('Joyful', 4, 1, 'According to the law of Moses, they brought Him up to Jerusalem to present Him to the Lord. (Luke 2:22)', 4, NULL),
        ('Joyful', 4, 2, 'Now there was a man in Jerusalem, whose name was Simeon, and this man was righteous and devout, looking for the consolation of Israel, and the Holy Spirit was upon him. (Luke 2:25)', 4, NULL),
        ('Joyful', 4, 3, 'And it had been revealed to him by the Holy Spirit that he should not see death before he had seen the Lord''s Christ. (Luke 2:26)', 4, NULL),
        ('Joyful', 4, 4, 'And when the parents brought in the child Jesus, he took Him up in his arms and blessed God. (Luke 2:27-28)', 4, NULL),
        ('Joyful', 4, 5, 'Lord, now lettest Thou Thy servant depart in peace, according to Thy word. (Luke 2:29)', 4, NULL),
        ('Joyful', 4, 6, 'For mine eyes have seen Thy salvation which Thou hast prepared before the face of all people. (Luke 2:30-31)', 4, NULL),
        ('Joyful', 4, 7, 'A light for revelation to the Gentiles, and for glory to Thy people Israel. (Luke 2:32)', 4, NULL),
        ('Joyful', 4, 8, 'And Simeon blessed them and said to Mary, His mother, "Behold, this child is set for the fall and rising of many in Israel, and for a sign that is spoken against." (Luke 2:34-35)', 4, NULL),
        ('Joyful', 4, 9, 'And a sword shall pierce through your own soul also, that the thoughts of many hearts may be revealed. (Luke 2:35)', 4, NULL),
        ('Joyful', 4, 10, 'And when they had performed everything according to the law of the Lord, they returned to Galilee, to their own city of Nazareth. (Luke 2:39)', 4, NULL),
        ('Joyful', 4, 11, NULL, 5, NULL),
        ('Joyful', 4, 12, NULL, 6, NULL),
        ('Joyful', 5, 0, '5. The Finding of Jesus in the Temple\nFruit: Joy of Finding Jesus', 3, NULL),
        ('Joyful', 5, 1, 'And when Jesus was twelve years old, they went up according to custom. (Luke 2:42)', 4, NULL),
        ('Joyful', 5, 2, 'And when the feast was ended, as they were returning, the boy Jesus stayed behind in Jerusalem. (Luke 2:43)', 4, NULL),
        ('Joyful', 5, 3, 'And when they did not find Him, they returned to Jerusalem, seeking Him. (Luke 2:45)', 4, NULL),
        ('Joyful', 5, 4, 'After three days they found Him in the Temple, sitting among the teachers, listening to them and asking them questions. (Luke 2:46)', 4, NULL),
        ('Joyful', 5, 5, 'And all who heard Him were amazed at His understanding and His answers. (Luke 2:47)', 4, NULL),
        ('Joyful', 5, 6, 'Son, why have you treated us so? Behold, your father and I have been looking for you anxiously. (Luke 2:48)', 4, NULL),
        ('Joyful', 5, 7, 'And He said to them, "Why were you looking for me? Did you not know that I must be in my Father''s house?" (Luke 2:49)', 4, NULL),
        ('Joyful', 5, 8, 'And they did not understand the saying which He spoke to them. (Luke 2:50)', 4, NULL),
        ('Joyful', 5, 9, 'And He went down with them and came to Nazareth, and was obedient to them. (Luke 2:51)', 4, NULL),
        ('Joyful', 5, 10, 'And Jesus increased in wisdom and in stature and in favor with God and man. (Luke 2:52)', 4, NULL),
        ('Joyful', 5, 11, NULL, 5, NULL),
        ('Joyful', 5, 12, NULL, 6, NULL),
        ('Joyful', 6, 0, NULL, 7, NULL),
        ('Joyful', 6, 1, NULL, 8, NULL),
        ('Joyful', 6, 2, NULL, 9, NULL),
        ('Joyful', 6, 3, NULL, 1, NULL),
        ('Joyful', 0, 0, NULL, 1, NULL),
        ('Joyful', 0, 1, NULL, 2, NULL),
        ('Joyful', 0, 2, NULL, 3, NULL),
        ('Joyful', 0, 3, 'For Faith', 4, NULL),
        ('Joyful', 0, 4, 'For Hope', 4, NULL),
        ('Joyful', 0, 5, 'For Charity', 4, NULL),
        ('Joyful', 0, 6, NULL, 5, NULL),
        ('Joyful', 0, 7, NULL, 6, NULL),
        ('Sorrowful', 0, 0, NULL, 1, NULL),
        ('Sorrowful', 0, 1, NULL, 2, NULL),
        ('Sorrowful', 0, 2, NULL, 3, NULL),
        ('Sorrowful', 0, 3, 'For Faith', 4, NULL),
        ('Sorrowful', 0, 4, 'For Hope', 4, NULL),
        ('Sorrowful', 0, 5, 'For Charity', 4, NULL),
        ('Sorrowful', 0, 6, NULL, 5, NULL),
        ('Sorrowful', 0, 7, NULL, 6, NULL),
        ('Sorrowful', 1, 0, '1. The Agony in the Garden\nFruit: Trust in God', 3, NULL),
        ('Sorrowful', 1, 1, 'Then Jesus went with them to a place called Gethsemane...He began to be sorrowful and troubled. (Matthew 26:36-37)', 4, NULL),
        ('Sorrowful', 1, 2, 'Then He said to them, "My soul is very sorrowful, even to death; remain here, and watch with me." (Matthew 26:38)', 4, NULL),
        ('Sorrowful', 1, 3, 'And He withdrew from them about a stone''s throw, and knelt down and prayed. (Luke 22:41)', 4, NULL),
        ('Sorrowful', 1, 4, 'Father, if Thou are willing, remove this cup from me; nevertheless, not my will, but Thine, be done. (Luke 22:42)', 4, NULL),
        ('Sorrowful', 1, 5, 'And there appeared to Him an angel from heaven, strengthening Him. (Luke 22:43)', 4, NULL),
        ('Sorrowful', 1, 6, 'And being in an agony, He prayed more earnestly. (Luke 22:44)', 4, NULL),
        ('Sorrowful', 1, 7, 'And His sweat became like great drops of blood falling down on the ground. (Luke 22:44)', 4, NULL),
        ('Sorrowful', 1, 8, 'And He came to the disciples and found them sleeping, and He said to Peter, "So, could you not watch with me one hour?" (Matthew 26:40)', 4, NULL),
        ('Sorrowful', 1, 9, 'Watch and pray that you may not enter into temptation. The spirit indeed is willing, but the flesh is weak. (Matthew 26:41)', 4, NULL),
        ('Sorrowful', 1, 10, 'Behold, the hour is at hand, and the Son of Man is being betrayed into the hands of sinners. (Matthew 26:45)', 4, NULL),
        ('Sorrowful', 1, 11, NULL, 5, NULL),
        ('Sorrowful', 1, 12, NULL, 6, NULL),
        ('Sorrowful', 2, 0, '2. The Scourging at the Pillar\nFruit: Purity', 3, NULL),
        ('Sorrowful', 2, 1, 'They bound Jesus and led Him away and delivered Him to Pilate. And Pilate asked Him, "Are you the King of the Jews?" (Mark 15:1-2)', 4, NULL),
        ('Sorrowful', 2, 2, 'Jesus answered, "My kingship is not of this world... You say that I am a king." (John 18:36)', 4, NULL),
        ('Sorrowful', 2, 3, 'For this I was born, and for this I came into the world, to bear witness to the truth. Everyone who belongs to the truth hears my voice. (John 18:37)', 4, NULL),
        ('Sorrowful', 2, 4, 'And Pilate said to the chief priests and the multitudes, "I find no crime in this man. I will therefore chastise him and release him." (Luke 23:4,16)', 4, NULL),
        ('Sorrowful', 2, 5, 'Then Pilate took Jesus and scourged Him. (John 19:1)', 4, NULL),
        ('Sorrowful', 2, 6, 'He was despised and rejected by men, a man of sorrows and acquainted with grief. (Isaiah 53:3)', 4, NULL),
        ('Sorrowful', 2, 7, 'Surely He has borne our griefs and carried our sorrows. (Isaiah 53:4)', 4, NULL),
        ('Sorrowful', 2, 8, 'But He was wounded for our transgressions, He was bruised for our iniquities. (Isaiah 53:5)', 4, NULL),
        ('Sorrowful', 2, 9, 'He was oppressed, and He was afflicted, yet He opened not His mouth. (Isaiah 53:7)', 4, NULL),
        ('Sorrowful', 2, 10, 'By oppression and judgment He was taken away. (Isaiah 53:8)', 4, NULL),
        ('Sorrowful', 2, 11, NULL, 5, NULL),
        ('Sorrowful', 2, 12, NULL, 6, NULL),
        ('Sorrowful', 3, 0, '3. The Crowning with Thorns\nFruit: Moral Courage', 3, NULL),
        ('Sorrowful', 3, 1, 'And then the soldiers led Him away inside the palace (that is, the praetorium); and they called together the whole battalion (Mark 15:16)', 4, NULL),
        ('Sorrowful', 3, 2, 'And they clothed Him in a purple cloak, and twisting together a crown of thorns, they put it on Him. (Mark 15:17)', 4, NULL),
        ('Sorrowful', 3, 3, 'And they began to salute Him, "Hail, King of the Jews!" (Mark 15:18)', 4, NULL),
        ('Sorrowful', 3, 4, 'And they struck Him on the head with a reed, and spat upon Him, and they knelt down in homage to Him. (Mark 15:19)', 4, NULL),
        ('Sorrowful', 3, 5, 'Pilate went out again, and said to them: Behold I am bringing Him out to you, that you may know that I find no crime in Him. (John 19:4)', 4, NULL),
        ('Sorrowful', 3, 6, 'So, Jesus came out wearing the crown of thorns and the purple robe. Pilate said to them, "Here is the man!" (John 19:5)', 4, NULL),
        ('Sorrowful', 3, 7, 'When the chief priests and the officers saw Him, they cried out, "Crucify Him! Crucify Him!" (John 19:6)', 4, NULL),
        ('Sorrowful', 3, 8, '"Why, what evil has He done?" But they kept crying out the more, "Crucify Him!" (John 19:7)', 4, NULL),
        ('Sorrowful', 3, 9, '"Shall I crucify your king?" and the chief priests answered, "We have no King but Ceasar" (John 19:12)', 4, NULL),
        ('Sorrowful', 3, 10, 'Then he handed Him over to them to be crucified. And so they took Jesus and led Him away. (John 19:16)', 4, NULL),
        ('Sorrowful', 3, 11, NULL, 5, NULL),
        ('Sorrowful', 3, 12, NULL, 6, NULL),
        ('Sorrowful', 4, 0, '4. The Carrying of the Cross\nFruit: Patience', 3, NULL),
        ('Sorrowful', 4, 1, 'And when they had mocked Him, they stripped Him of the robe, and put His own clothes on Him, and led Him away to crucify Him. (Matthew 27:31)', 4, NULL),
        ('Sorrowful', 4, 2, 'And Jesus said to all, "If any man would come after me, let Him deny himself and take up his cross daily and follow Me." (Luke 9:23)', 4, NULL),
        ('Sorrowful', 4, 3, 'For whoever would save his life will lose it, and whoever loses his life for my sake will find it. (Luke 9:24)', 4, NULL),
        ('Sorrowful', 4, 4, 'And bearing the cross for Himself, He went out to the place called "The Skull". (John 19:17)', 4, NULL),
        ('Sorrowful', 4, 5, 'And as they led Him away, they seized on Simone of Cyrene, who was coming in from the country, and laid the cross on him to carry it behind Jesus. (Luke 23:26)', 4, NULL),
        ('Sorrowful', 4, 6, 'Take my yoke upon you, and learn from me, for I am gentle and lowly in heart, and you will find rest for your souls. (Matthew 11:29)', 4, NULL),
        ('Sorrowful', 4, 7, 'For my yoke is easy, and my burden is light. (Matthew 11:30)', 4, NULL),
        ('Sorrowful', 4, 8, 'And there followed Him a great multitude of people, and of women who bewailed and lamented Him. (Luke 23:27)', 4, NULL),
        ('Sorrowful', 4, 9, 'But Jesus turning to them said, "Daughters of Jerusalem, do not weep for me, but weep for yourselves and for your children." (Luke 23:28)', 4, NULL),
        ('Sorrowful', 4, 10, 'For if they do this when the wood is green, what will happen when it is dry? (Luke 23:31)', 4, NULL),
        ('Sorrowful', 4, 11, NULL, 5, NULL),
        ('Sorrowful', 4, 12, NULL, 6, NULL),
        ('Sorrowful', 5, 0, '5. The Crucifixion\nFruit: Forgiveness', 3, NULL),
        ('Sorrowful', 5, 1, 'And when they came to the place which is called "The Skull", there they crucified Him. (Luke 23:33)', 4, NULL),
        ('Sorrowful', 5, 2, 'And Jesus said, "Father, forgive them, for they know not what they do." (Luke 23:34)', 4, NULL),
        ('Sorrowful', 5, 3, 'And he said, "Jesus, remember me when you come into your kingdom." (Luke 23:42)', 4, NULL),
        ('Sorrowful', 5, 4, 'And He said to him, "Truly, I say to you, today you will be with me in Paradise." (Luke 23:43)', 4, NULL),
        ('Sorrowful', 5, 5, 'And Jesus saw His mother, and the disciple standing by, whom He loved. (John 19:26)', 4, NULL),
        ('Sorrowful', 5, 6, 'And He said to His mother, "Woman behold your son." Then He said to His disciple, "Behold your mother." (John 19:26-27)', 4, NULL),
        ('Sorrowful', 5, 7, 'And from that hour, the disciple took her into his home. (John 19:27)', 4, NULL),
        ('Sorrowful', 5, 8, 'The sun''s light failed; and the curtain of the temple was torn in two. (Luke 23:45)', 4, NULL),
        ('Sorrowful', 5, 9, 'Then Jesus, crying with a loud voice, said, "Father, into your hands I commend my spirit." (Luke 23:46)', 4, NULL),
        ('Sorrowful', 5, 10, 'And having said this, He breathed His last. (Luke 23:46)', 4, NULL),
        ('Sorrowful', 5, 11, NULL, 5, NULL),
        ('Sorrowful', 5, 12, NULL, 6, NULL),
        ('Sorrowful', 6, 0, NULL, 7, NULL),
        ('Sorrowful', 6, 1, NULL, 8, NULL),
        ('Sorrowful', 6, 2, NULL, 9, NULL),
        ('Sorrowful', 6, 3, NULL, 1, NULL)
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
    /** 
      stmt.execute("""
          DROP TABLE ROSARY
          """);
      stmt.execute("""
          DROP TABLE PRAYERS
          """); 
      stmt.executeUpdate(CREATE_PRAYERS);
      stmt.executeUpdate(CREATE_ROSARY); */
      int inserted = stmt.executeUpdate(INSERT_ROSARIES);
      System.out.println(inserted);
    }
  }
}