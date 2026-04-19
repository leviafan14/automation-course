package auto;

// Конфигурация БД
interface DbConfig extends org.aeonbits.owner.Config {
    @Key("db.url")
    @DefaultValue("jdbc:postgresql://localhost:5433/test_db")
    String dbUrl();

    @Key("db.user")
    @DefaultValue("admin")
    String dbUser();

    @Key("db.password")
    @DefaultValue("secret")
    String dbPassword();
}
