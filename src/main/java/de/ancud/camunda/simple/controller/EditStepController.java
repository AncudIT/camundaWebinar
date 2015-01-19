package de.ancud.camunda.simple.controller;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import java.util.*;

@Controller
@RequestMapping("view")
public class EditStepController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    private RuntimeService runtimeService;

    @RenderMapping(params = "show=Task")
    public ModelAndView showTask(ModelMap modelMap,
                                 @RequestParam(required = false) String taskId,
                                 @RequestParam(required = false) String processDefinitionId,
                                 RenderRequest renderRequest) throws PortalException, SystemException {
        ModelAndView modelAndView = new ModelAndView("editStep");
        User user = PortalUtil.getUser(renderRequest);
        if (user == null) {
            // user is not logged in
            ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
            modelMap.put("loggedin", Boolean.FALSE);
            modelMap.put("signinUrl", themeDisplay.getURLSignIn());
        }
        else {
            // user is logged in
            modelMap.put("loggedin", Boolean.TRUE);
            modelMap.put("userid", user.getUserId());

            modelMap.put("processDefinitionId", processDefinitionId);
            modelMap.put("taskId", taskId);

            Map<String, Object> vars = new HashMap<String, Object>();
            List<FormField> formFields;
            if(taskId != null) {
                vars = taskService.getVariables(taskId);
                formFields = getFormFields(taskId);
            } else {
                formFields = getFormFieldsProcessStart(processDefinitionId);
            }
            modelMap.put("taskVars", vars);
            modelMap.put("formFields", formFields);

            // Remove editable formFields from TaskVars that are just displayed
            Map<String, Object> taskVarsNotForEdit = new HashMap<String, Object>(vars);
            Iterator<FormField> iterator = formFields.iterator();
            while(iterator.hasNext()) {
                String id = iterator.next().getId();
                if(taskVarsNotForEdit.containsKey(id))
                    taskVarsNotForEdit.remove(id);
            }
            modelMap.put("taskVarsNotForEdit", taskVarsNotForEdit);
        }
        return modelAndView;
    }

    /**
     * Get the Form fields which should be displayed modifiable in UI.
     *
     * @param taskId String id of the Task
     * @return List of FormField's
     */
    private List<FormField> getFormFields(String taskId) {
        return formService.getTaskFormData(taskId).getFormFields();
    }

    private List<FormField> getFormFieldsProcessStart(String processDefinitionId) {
        return formService.getStartFormData(processDefinitionId).getFormFields();
    }

    @ActionMapping("commitTask")
    public void commitTask(ActionRequest actReq, ActionResponse actResp,
                           @RequestParam(required = false) String taskId,
                           @RequestParam(required = false) String processDefinitionId
                           ) {
        if(taskId != null && taskId.length() > 0) {
            List<FormField> formFields = getFormFields(taskId);
            Map<String, Object> commitValues = fillFormVariables(actReq, formFields);
            taskService.complete(taskId, commitValues);
        } else {
            List<FormField> formFields = getFormFieldsProcessStart(processDefinitionId);
            Map<String, Object> commitValues = fillFormVariables(actReq, formFields);
            runtimeService.startProcessInstanceById(processDefinitionId, commitValues);
        }
    }

    private Map<String, Object> fillFormVariables(ActionRequest actReq, List<FormField> formFields) {
        Map<String, Object> commitValues = new LinkedHashMap<String, Object>();
        for (FormField field : formFields) {
            String formString = actReq.getParameter(field.getId());
            FormType type = field.getType();
            Object value;
            if(type instanceof AbstractFormFieldType) {
                AbstractFormFieldType abstractFormFieldType = (AbstractFormFieldType)type;
                value = ((AbstractFormFieldType) type).convertFormValueToModelValue(formString);
            } else {
                value = formString;
            }
            commitValues.put(field.getId(), value);
        }
        return commitValues;
    }
}
