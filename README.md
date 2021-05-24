## Campo gerado automaticamente para MongoDB usando Spring Boot

# 1. Visão Geral
Neste tutorial, vamos aprender como implementar um campo sequencial gerado automaticamente para MongoDB no Spring Boot.

Quando usamos o MongoDB como banco de dados para um aplicativo Spring Boot, não podemos usar a anotação @GeneratedValue em nossos modelos, pois ela não está disponível. Portanto, precisamos de um método para produzir o mesmo efeito que teríamos se estivermos usando JPA e um banco de dados SQL.

A solução geral para esse problema é simples. Vamos criar uma coleção (tabela) que armazenará a sequência gerada para outras coleções. Durante a criação de um novo registro, vamos usá-lo para buscar o próximo valor.

# 2. Dependências
Vamos adicionar os seguintes iniciadores de inicialização rápida ao nosso pom.xml:

```
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <versionId>2.2.2.RELEASE</versionId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
        <versionId>2.2.2.RELEASE</versionId>
    </dependency>
</dependencies>
```

A versão mais recente para as dependências é gerenciada por spring-boot-starter-parent.

# 3. Coleções
Conforme discutido na visão geral, criaremos uma coleção que armazenará a sequência incrementada automaticamente para outras coleções. Chamaremos essa coleção de database_sequences. Ele pode ser criado usando o shell mongo ou o MongoDB Compass. Vamos criar uma classe de modelo correspondente:

```
@Document(collection = "database_sequences")
public class DatabaseSequence {

    @Id
    private String id;

    private long seq;

    //getters and setters omitted
}
```

Vamos então criar uma coleção de usuários e um objeto de modelo correspondente, que armazenará os detalhes das pessoas que estão usando nosso sistema:

```
@Document(collection = "users")
public class User {

    @Transient
    public static final String SEQUENCE_NAME = "users_sequence";

    @Id
    private long id;

    private String email;

    //getters and setters omitted
}
```

No modelo de usuário criado acima, adicionamos um campo estático SEQUENCE_NAME, que é uma referência única para a sequência auto-incrementada para a coleção de usuários.

Também anotamos com @Transient para evitar que seja persistido ao lado de outras propriedades do modelo.

# 4. Criação de um novo registro
Até agora, criamos as coleções e modelos necessários. Agora, vamos criar um serviço que irá gerar o valor auto-incrementado que pode ser usado como id para nossas entidades.

Vamos criar um SequenceGeneratorService que tem generateSequence():

```
public long generateSequence(String seqName) {
    DatabaseSequence counter = mongoOperations.findAndModify(query(where("_id").is(seqName)),
      new Update().inc("seq",1), options().returnNew(true).upsert(true),
      DatabaseSequence.class);
    return !Objects.isNull(counter) ? counter.getSeq() : 1;
}
```

Agora, podemos usar o generateSequence() ao criar um novo registro:

```
User user = new User();
user.setId(sequenceGenerator.generateSequence(User.SEQUENCE_NAME));
user.setEmail("john.doe@example.com");
userRepository.save(user);
```

Para listar todos os usuários, usaremos o UserRepository:

```
List<User> storedUsers = userRepository.findAll();
storedUsers.forEach(System.out::println);
```

Como está agora, temos que definir o campo id toda vez que criamos uma nova instância de nosso modelo. Podemos contornar esse processo criando um ouvinte para eventos de ciclo de vida do Spring Data MongoDB.

Para fazer isso, criaremos um UserModelListener que estende AbstractMongoEventListener ```<User>``` e, em seguida, substituiremos o onBeforeConvert():

```
@Override
public void onBeforeConvert(BeforeConvertEvent<User> event) {
    if (event.getSource().getId() < 1) {
        event.getSource().setId(sequenceGenerator.generateSequence(User.SEQUENCE_NAME));
    }
}
```

Agora, toda vez que salvarmos um novo usuário, o id será configurado automaticamente.

# 5. Conclusão
Concluindo, vimos como gerar valores sequenciais e auto-incrementados para o campo id e simular o mesmo comportamento visto em bancos de dados SQL.

O Hibernate usa um método semelhante para gerar valores auto-incrementados por padrão.