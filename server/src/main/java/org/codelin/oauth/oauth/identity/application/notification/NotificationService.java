package org.codelin.oauth.oauth.identity.application.notification;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容（HTML）
     */
    void sendEmail(String to, String subject, String content);

    /**
     * 发送短信
     *
     * @param phone   手机号
     * @param content 内容
     */
    void sendSms(String phone, String content);

    /**
     * 使用模板发送邮件
     *
     * @param to         收件人
     * @param templateCode 模板编码
     * @param variables  模板变量
     */
    void sendEmailByTemplate(String to, String templateCode, java.util.Map<String, String> variables);
}
