package org.codelin.oauth.oauth.identity.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.identity.domain.model.EmailTemplate;
import org.codelin.oauth.oauth.identity.infrastructure.persistence.EmailTemplateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 邮件模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateMapper emailTemplateMapper;

    /**
     * 获取模板
     */
    public EmailTemplate getByCode(String code) {
        return emailTemplateMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("code", code)
        );
    }

    /**
     * 渲染模板
     *
     * @param code      模板编码
     * @param variables 模板变量
     * @return 渲染后的HTML内容
     */
    public String render(String code, Map<String, String> variables) {
        EmailTemplate template = getByCode(code);
        if (template == null) {
            throw BizException.notFound("error.email.template-not-found");
        }

        String content = template.getContent();

        // 替换变量
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("${" + entry.getKey() + "}", entry.getValue());
            }
        }

        // 替换系统变量
        content = content.replace("${year}", String.valueOf(LocalDate.now().getYear()));

        return content;
    }

    /**
     * 渲染主题
     *
     * @param code      模板编码
     * @param variables 模板变量
     * @return 渲染后的主题
     */
    public String renderSubject(String code, Map<String, String> variables) {
        EmailTemplate template = getByCode(code);
        if (template == null) {
            throw BizException.notFound("error.email.template-not-found");
        }

        String subject = template.getSubject();

        // 替换变量
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                subject = subject.replace("${" + entry.getKey() + "}", entry.getValue());
            }
        }

        return subject;
    }

    /**
     * 获取所有模板
     */
    public List<EmailTemplate> listAll() {
        return emailTemplateMapper.selectAll();
    }

    /**
     * 更新模板
     */
    public void update(Long id, EmailTemplate update) {
        EmailTemplate existing = emailTemplateMapper.selectOneById(id);
        if (existing == null) {
            throw BizException.notFound("error.email.template-not-found");
        }

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getSubject() != null) {
            existing.setSubject(update.getSubject());
        }
        if (update.getContent() != null) {
            existing.setContent(update.getContent());
        }
        if (update.getVariables() != null) {
            existing.setVariables(update.getVariables());
        }

        emailTemplateMapper.update(existing);
    }
}
