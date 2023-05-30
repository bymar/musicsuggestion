package cesarschool.musicsuggestion;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static cesarschool.musicsuggestion.Constants.*;

public class MusicSuggestion {
    private static final ConnectionFactory factory = new ConnectionFactory();
    private static Connection connection;
    private static Channel channel;

    public static void main(String[] args) throws IOException, TimeoutException {
        configQueue();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("<=== Select Option: ===>");
            System.out.println("<==== 1. Producer =====>");
            System.out.println("<==== 2. Consumer =====>");
            System.out.println("<====== 3. Audit ======>");
            System.out.println("<====== 4. Exit =======>");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1 -> producer(scanner);
                case 2 -> consumer(scanner);
                case 3 -> auditMenu(scanner);
                case 4 -> {
                    channel.close();
                    connection.close();
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid Option. Please try again");
            }
        }
    }

    private static void producer(Scanner scanner) throws IOException {
        System.out.println("Musical Genre(s), if more than one separate by ',':");
        String producerName = scanner.nextLine();

        System.out.println("[Name of the Song] - [Artist]:");
        String message = scanner.nextLine();

        String[] topics = producerName.split(",");

        for (String topic : topics) {
            String formattedMessage = formatMessage(producerName, message);
            // Pública a mensagem já formatada para a fila
            channel.basicPublish(EXCHANGE_NAME, topic.trim(), null, formattedMessage.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("Message Published");
    }

    /**
     * Método para a formatação da mensagem que está sendo enviada
     * trazendo a data e horario atual, juntando com nome do produtor e sua mensagem
     */
    private static String formatMessage(String producerName, String message) {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String formattedDate = dateTime.format(formatter);

        return "[" + formattedDate + "] " + producerName + ": " + message;
    }

    private static void consumer(Scanner scanner) throws IOException {
        System.out.println("Who is listing?");
        String consumerName = scanner.nextLine();

        System.out.println("Which music genre(s) you want to receive suggestions?" +
                "\n" + "(Separate them by ',')");
        String topicString = scanner.nextLine();
        String[] topics = topicString.split(",");

        String queueName = channel.queueDeclare().getQueue();

        // Após receber o tópico desejado pelo usuário, faz uma procura por ele no exchange
        for (String topic : topics) {
            channel.queueBind(queueName, EXCHANGE_NAME, topic.trim());
        }

        System.out.println("Waiting for suggestions. Press ENTER to exit.");

        try {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Music suggestion received: " + message);
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scanner.nextLine();
    }

    /**
     * Método de configuração para a auditoria, tendo uma fila do Rabbit
     * para o recebimento dessas mensagens.
     * Mensagens enviadas quando não tinha nenhum consumidor ativo, ficam 
     * dentro dessa fila, até que este método seja chamado.
     */
    private static void auditMenu(Scanner scanner) {
        System.out.println("Waiting for messages in audit queue.");

        try {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Messages received: " + message);
                }
            };
            channel.basicConsume(AUDIT_QUEUE_NAME, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scanner.nextLine();
    }

    /**
     * Configuração do RabbitMQ, conexão com Exchange, com as
     * filas. 
     */
    private static void configQueue() throws IOException, TimeoutException {
        factory.setHost("localhost");

        // Cria uma conexão
        connection = factory.newConnection();
        channel = connection.createChannel();
        // Declara o exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        // Declara o qual a fila que vai receber as mensagens e passa os paramentos que ela vai ter e como ela vai se comportar
        channel.queueDeclare(AUDIT_QUEUE_NAME, true, false, false, null);
        // Conexão entre fila e exchange 
        channel.queueBind(AUDIT_QUEUE_NAME, EXCHANGE_NAME, "#");

    }
}
