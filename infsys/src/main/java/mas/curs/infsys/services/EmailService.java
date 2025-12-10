package mas.curs.infsys.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookReleaseNotification(String toEmail, String bookTitle, String bookUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Книга из вашего списка желаний вышла!");
            message.setText("Здравствуйте!\n\n" +
                    "Книга \"" + bookTitle + "\" из вашего списка желаний теперь доступна!\n\n" +
                    "Посмотреть книгу: " + bookUrl + "\n\n" +
                    "С уважением,\n" +
                    "Команда ИД \"Инженерия Данных\"");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

    public void sendDeletionWarning(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Ваш аккаунт будет удален через месяц");
            message.setText("Здравствуйте, " + username + "!\n\n" +
                    "Мы заметили, что вы не заходили в систему более 11 месяцев.\n\n" +
                    "Согласно политике нашей системы, неактивные аккаунты удаляются через год после последнего входа.\n" +
                    "Ваш аккаунт будет автоматически удален через месяц, если вы не войдете в систему.\n\n" +
                    "Чтобы сохранить свой аккаунт, просто войдите в систему в течение ближайшего месяца.\n\n" +
                    "С уважением,\n" +
                    "Команда ИД \"Инженерия Данных\"");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send deletion warning email to " + toEmail + ": " + e.getMessage());
        }
    }

    public void sendTestEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Тестовое письмо - Проверка рассылки");
            message.setText("Здравствуйте, " + username + "!\n\n" +
                    "Это тестовое письмо для проверки работы системы рассылки.\n\n" +
                    "Если вы получили это письмо, значит система отправки email-уведомлений работает корректно.\n\n" +
                    "С уважением,\n" +
                    "Команда ИД \"Инженерия Данных\"");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send test email to " + toEmail + ": " + e.getMessage());
            throw e;
        }
    }
}

