package com.erzbir.xiaobei;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * @Author: Erzbir
 * @Date: 2022/9/1 18:05
 * @<code> 推送消息的类 </code>
 */
public class SendMessage {
    private User user;
    private String sender;
    private String key;
    private String host;

    public SendMessage(User user) throws FileNotFoundException {
        // 默认解析在config.json中的设置
        this(user, new FileReader("config.json"));
    }

    public SendMessage(User user, String sender, String key, String host) {
        this.user = user;
        this.sender = sender;
        this.key = key;
        this.host = host;
    }

    public SendMessage(User user, FileReader fileReader) {
        JsonObject jsonObject;
        jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
        if (jsonObject == null) {
            return;
        }
        this.user = user;
        sender = jsonObject.get("sender_email").getAsString();
        key = jsonObject.get("key").getAsString();
        host = jsonObject.get("host").getAsString();
    }

    /**
     *
     */
    public void send_email(String content) {
        System.out.println(this);
        if (sender == null
                || content == null
                || sender.isEmpty()
                || key == null
                || key.isEmpty()
                || user == null
                || user.getACCEPT_EMAIL() == null
                || user.getACCEPT_EMAIL().isEmpty()
                || host == null
                || host.isEmpty()) {
            System.out.println("邮件推送必要属性为空");
            return;
        }
        String account = sender.substring(0, sender.indexOf("@"));
        if (account.isEmpty()) {
            System.out.println("邮箱服务帐号为空");
            return;
        }
        Properties properties = new Properties();
        Transport transport = null;
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.host", host);
        properties.setProperty("mail.user", account);
        properties.setProperty("mail.from", sender);
        properties.put("mail.smtp.connectiontimeout", "3000");// 设置接收超时时间
        properties.put("mail.smtp.timeout", "3000");// 设置读取超时时间
        properties.put("mail.smtp.writetimeout", "3000");// 设置写入超时时间
        Session session = Session.getInstance(properties, null);
        MimeMessage message = new MimeMessage(session);
        try {
            String nick = "";
            try {
                nick = javax.mail.internet.MimeUtility.encodeText("自动打卡反馈");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            message.setFrom(new InternetAddress(nick + "<" + sender + ">"));
            InternetAddress to = new InternetAddress(user.getACCEPT_EMAIL());
            message.setRecipient(Message.RecipientType.TO, to);
            message.setSubject("自动打卡反馈");
            message.setContent(content, "text/plain;charset=UTF-8");
            message.saveChanges();
            transport = session.getTransport();
            transport.connect(null, null, key);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("邮件推送失败");
            return;
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("邮件推送成功");
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "SendMessage{" +
                "user=" + user +
                ", sender='" + sender + '\'' +
                ", key='" + key + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
