/**
 * 
 */

package org.nuxeo.sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.operations.services.DocumentPageProviderOperation;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.collections.api.CollectionConstants;
import org.nuxeo.ecm.collections.api.CollectionManager;
import org.nuxeo.ecm.collections.core.automation.SuggestCollectionEntry;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.ecm.platform.ui.select2.common.Select2Common;

/**
 * @author harlan
 */
@Operation(id=SuggestFileNumber.ID, category=Constants.CAT_SERVICES, label="SuggestFileNumber", description="")
public class SuggestFileNumber {

    public static final String ID = "SuggestFileNumber";
    private static final String PATH = "path";

    @Param(name = "currentPageIndex", required = false)
    protected Integer currentPageIndex = 0;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize = 20;

    @Context
    protected AutomationService service;

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Context
    protected CollectionManager collectionManager;

    @Param(name = "lang", required = false)
    protected String lang;

    @Param(name = "searchTerm", required = false)
    protected String searchTerm;

    private final static Log log = LogFactory.getLog(SuggestCollectionEntry.class);

    @OperationMethod
    public Blob run() throws Exception {
    	JSONArray result = new JSONArray();
    	
        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY,
                (Serializable) session);

        Map<String, Object> vars = ctx.getVars();

        StringList sl = new StringList();
        sl.add(searchTerm + (searchTerm.endsWith("%") ? "" : "%"));
//        sl.add(DocumentPageProviderOperation.CURRENT_USERID_PATTERN);
        vars.put("queryParams", sl);
        vars.put("providerName", "FILE_NUMBER_SUGGESTION");
        OperationContext subctx = new OperationContext(ctx.getCoreSession(),
                vars);
        OperationChain chain = new OperationChain("operation");
        OperationParameters oparams = new OperationParameters(
                DocumentPageProviderOperation.ID, vars);
        chain.add(oparams);
        @SuppressWarnings("unchecked")
        List<DocumentModel> docs = (List<DocumentModel>) service.run(subctx,
                chain);

        boolean found = false;
        for (DocumentModel doc : docs) {
            JSONObject obj = new JSONObject();
            obj.element(Select2Common.ID, doc.getId());
            found = true;
            obj.element(Select2Common.LABEL, doc.getProperty("dublincore","description"));
            if(!result.contains(obj)){
                result.add(obj);
            }
        }

        // allows one to add a value not found
//        if (!found && StringUtils.isNotBlank(searchTerm)) {
//            JSONObject obj = new JSONObject();
//            obj.element(Select2Common.LABEL, searchTerm);
//            obj.element(Select2Common.ID, CollectionConstants.MAGIC_PREFIX_ID
//                    + searchTerm);
//            result.add(0, obj);
//        }
        
        log.error(result.toString());
    	return new StringBlob(result.toString(), "application/json");

    }    
    protected Locale getLocale() {
        return new Locale(getLang());
    }

    protected String getLang() {
        if (lang == null) {
            lang = (String) ctx.get("lang");
            if (lang == null) {
                lang = Select2Common.DEFAULT_LANG;
            }
        }
        return lang;
    }

}
