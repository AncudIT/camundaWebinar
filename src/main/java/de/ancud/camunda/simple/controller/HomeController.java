package de.ancud.camunda.simple.controller;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import de.ancud.camunda.simple.log.ModuleLogger;
import org.apache.commons.logging.Log;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import javax.portlet.RenderRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("view")
public class HomeController {

    private static final Log LOG = ModuleLogger.getLogger(HomeController.class);
    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;
    @Autowired
    private FormattingConversionService conversionService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;

    @RenderMapping
    public ModelAndView showProjekte(ModelMap modelMap, RenderRequest renderRequest) throws Exception {
        ModelAndView modelAndView = new ModelAndView("home");
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
            modelMap.put("tasks", getTaskList(user));
            modelMap.put("files", repositoryService.createProcessDefinitionQuery().latestVersion().orderByProcessDefinitionName().asc().list());
        }
        modelAndView.addObject(modelMap);
        return modelAndView;
    }

    public List<Task> getTaskList(User currentUser) {
        LinkedList<Task> results = new LinkedList<Task>();
        List<Task> result = null;
        try {
            result = taskService.createTaskQuery().taskAssignee(currentUser.getLogin()).orderByTaskId().asc().list();
            results.addAll(result);
        } catch (PortalException e) {
            LOG.warn("Unable to retrieve User Login information, task assigned to user will not be shown", e);
        } catch (SystemException e) {
            LOG.error("Not possible to retrieve User Login information", e);
        }

        try {
            long[] groups = currentUser.getUserGroupIds();
            List<String> strGroups = new LinkedList<String>();
            for (long group : groups) {
                strGroups.add(Long.toString(group));
            }
            TaskQuery taskQuery = taskService.createTaskQuery();
            if (!strGroups.isEmpty()) {
                taskQuery = taskQuery.taskCandidateGroupIn(strGroups);
            }
            result = taskQuery.orderByTaskId().asc().list();
            results.addAll(result);
        } catch (SystemException e) {
            LOG.error("Unable to retrieve GroupIds of current user, Group tasks will not be shown", e);
        }
        if (results.isEmpty()) {
            LOG.info("No tasks found for current user");
            results.addAll(taskService.createTaskQuery().orderByTaskId().asc().list());
        } else {
            Collections.sort(results, new Comparator<Task>() {
                @Override
                public int compare(Task o1, Task o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
        }
        return results;
    }

    @ResourceMapping("getBpmn")
    public void getBpmn(ResourceResponse response, @RequestParam(required = true) String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task != null) {
            try {
                OutputStream outputStream = response.getPortletOutputStream();
                BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(task.getProcessDefinitionId());
                Bpmn.writeModelToStream(outputStream, bpmnModelInstance);
            } catch (IOException e) {
                LOG.error("No outstream...");
            }
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setConversionService(conversionService);
        binder.setValidator(localValidatorFactoryBean);
    }
}
