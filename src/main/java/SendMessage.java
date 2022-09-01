import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @Author: Erzbir
 * @Date: 2022/9/1 18:05
 * <p>
 * 发送消息
 */
public class SendMessage {
    private volatile static SendMessage singTon = null;
    private String host = "smtp.qq.cpm";
    private String user = "2978086497";
    private String sender = Reference.EMAIL;
    private String key = "ekkoxewokvgodhea";
    private String receiver = Reference.ACCEPT_EMAIL;

    private SendMessage() {

    }

    public static SendMessage getInstance() {
        if (singTon == null) {
            synchronized (SendMessage.class) {
                if (singTon == null) {
                    singTon = new SendMessage();
                }
            }
        }
        return singTon;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean send_email(String content) {
        if (sender == null || sender.length() == 0 || user == null || receiver == null
                || user.length() == 0 || receiver.length() == 0) {
            return false;
        }
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.host", host);
        properties.setProperty("mail.user", user);
        properties.setProperty("mail.from", sender);
        Session session = Session.getInstance(properties, null);
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        try {
            InternetAddress from = new InternetAddress(sender);
            message.setFrom(from);
            InternetAddress to = new InternetAddress(receiver);
            message.setRecipient(Message.RecipientType.TO, to);
            message.setSubject("自动打卡反馈");
            message.setContent(content, "text/plain;charset=UTF-8");
            message.saveChanges();
            Transport transport = session.getTransport();
            transport.connect(null, null, key);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
}
