package org.opencps.communication.service.indexer;

import java.util.LinkedHashMap;
import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.opencps.communication.constants.NotificationTemplateTerm;
import org.opencps.communication.model.Notificationtemplate;
import org.opencps.communication.service.NotificationtemplateLocalServiceUtil;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexWriterHelperUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

public class NotificationTemplateIndexer extends BaseIndexer<Notificationtemplate> {

	public static final String CLASS_NAME = Notificationtemplate.class.getName();

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public void postProcessSearchQuery(BooleanQuery searchQuery, BooleanFilter fullQueryBooleanFilter,
			SearchContext searchContext) throws Exception {

		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.NOTIFICATIONTEMPLATE_ID, false);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.GROUP_ID, false);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.COMPANY_ID, false);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.USER_NAME, false);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.USER_ID, false);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.CREATE_DATE, false);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.MODIFIED_DATE, false);

		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.NOTIFICATTION_TYPE, true);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.NOTIFICATION_EMAIL_SUBJECT, true);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.NOTIFICATION_EMAIL_BODY, true);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.NOTIFICATION_TEXT_MESSAGE, true);
		addSearchTerm(searchQuery, searchContext, NotificationTemplateTerm.NOTIFICATION_TEXT_SMS, true);

		LinkedHashMap<String, Object> params = (LinkedHashMap<String, Object>) searchContext.getAttribute("params");

		if (params != null) {
			String expandoAttributes = (String) params.get("expandoAttributes");

			if (Validator.isNotNull(expandoAttributes)) {
				addSearchExpando(searchQuery, searchContext, expandoAttributes);
			}
		}
	}

	@Override
	protected void doDelete(Notificationtemplate nofificationTemplae) throws Exception {
		deleteDocument(nofificationTemplae.getCompanyId(), nofificationTemplae.getNotificationTemplateId());
	}

	@Override
	protected Document doGetDocument(Notificationtemplate notificationtemplates) throws Exception {
		
		Document document = getBaseModelDocument(CLASS_NAME, notificationtemplates);	

		document.addNumberSortable(NotificationTemplateTerm.GROUP_ID, notificationtemplates.getGroupId());		
		document.addKeywordSortable(Field.COMPANY_ID, String.valueOf(notificationtemplates.getCompanyId()));
		document.addDateSortable(Field.MODIFIED_DATE, notificationtemplates.getModifiedDate());
		document.addKeywordSortable(Field.USER_ID, String.valueOf(notificationtemplates.getUserId()));
		document.addKeywordSortable(Field.USER_NAME, String.valueOf(notificationtemplates.getUserName()));

		document.addTextSortable(NotificationTemplateTerm.NOTIFICATTION_TYPE, notificationtemplates.getNotificationType());		
		document.addNumberSortable(NotificationTemplateTerm.GROUP_ID, notificationtemplates.getGroupId());
		document.addNumberSortable(NotificationTemplateTerm.USER_ID, notificationtemplates.getUserId());
		document.addNumberSortable(NotificationTemplateTerm.COMPANY_ID, notificationtemplates.getCompanyId());
		document.addNumberSortable(NotificationTemplateTerm.NOTIFICATIONTEMPLATE_ID, notificationtemplates.getNotificationTemplateId());
		
		document.addTextSortable(NotificationTemplateTerm.NOTIFICATION_EMAIL_SUBJECT, notificationtemplates.getEmailSubject());
		document.addTextSortable(NotificationTemplateTerm.NOTIFICATION_EMAIL_BODY, notificationtemplates.getEmailBody());
		document.addTextSortable(NotificationTemplateTerm.NOTIFICATION_TEXT_MESSAGE, notificationtemplates.getTextMessage());
		document.addTextSortable(NotificationTemplateTerm.NOTIFICATION_TEXT_SMS, notificationtemplates.getTextSMS());

		document.setSortableTextFields(
				new String[] { NotificationTemplateTerm.NOTIFICATTION_TYPE,NotificationTemplateTerm.NOTIFICATION_EMAIL_SUBJECT});

		return document;
	}

	@Override
	protected String doGetSortField(String orderByCol) {
		if (orderByCol.equals("email-address")) {
			return "emailAddress";
		} else if (orderByCol.equals("first-name")) {
			return "firstName";
		} else if (orderByCol.equals("job-title")) {
			return "jobTitle";
		} else if (orderByCol.equals("last-name")) {
			return "lastName";
		} else {
			return orderByCol;
		}
	}

	@Override
	protected Summary doGetSummary(Document document, Locale locale, String snippet, PortletRequest portletRequest,
			PortletResponse portletResponse) {

		Summary summary = createSummary(document);

		summary.setMaxContentLength(QueryUtil.ALL_POS);

		return summary;
	}

	@Override
	protected void doReindex(Notificationtemplate nofificationTemplae) throws Exception {
		Document document = getDocument(nofificationTemplae);

		IndexWriterHelperUtil.updateDocument(getSearchEngineId(), nofificationTemplae.getCompanyId(), document,
				isCommitImmediately());
	}

	@Override
	protected void doReindex(String className, long classPK) throws Exception {
		Notificationtemplate nofificationTemplae = NotificationtemplateLocalServiceUtil.fetchNotificationtemplate(classPK);

		doReindex(nofificationTemplae);
	}

	@Override
	protected void doReindex(String[] ids) throws Exception {
		long companyId = GetterUtil.getLong(ids[0]);

		reindexNotificationtemplate(companyId);
	}

	protected void reindexNotificationtemplate(long companyId) throws PortalException {
		final IndexableActionableDynamicQuery indexableActionableDynamicQuery = NotificationtemplateLocalServiceUtil
				.getIndexableActionableDynamicQuery();

		indexableActionableDynamicQuery.setCompanyId(companyId);
		indexableActionableDynamicQuery
				.setPerformActionMethod(new ActionableDynamicQuery.PerformActionMethod<Notificationtemplate>() {

					@Override
					public void performAction(Notificationtemplate nofificationTemplate) {
						try {
							Document document = getDocument(nofificationTemplate);

							indexableActionableDynamicQuery.addDocuments(document);
						} catch (PortalException pe) {
							if (_log.isWarnEnabled()) {
								_log.warn("Unable to index contact " + nofificationTemplate.getNotificationTemplateId(), pe);
							}
						}
					}

				});
		indexableActionableDynamicQuery.setSearchEngineId(getSearchEngineId());

		indexableActionableDynamicQuery.performActions();
	}

	private static final Log _log = LogFactoryUtil.getLog(NotificationTemplateIndexer.class);

}