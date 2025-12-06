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
}

