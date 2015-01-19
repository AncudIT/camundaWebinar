package de.ancud.camunda.simple.controller;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.application.impl.EmbeddedProcessApplicationReferenceImpl;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.ActionRequest;

@Controller
@RequestMapping("edit")
public class EditController {
    @Autowired
    private RepositoryService repositoryService;
	@Autowired
	private ApplicationContext applicationContext;

	@RenderMapping
	public ModelAndView showBpmnFiles() {
		ModelAndView modelAndView = new ModelAndView("edit");
		modelAndView.addObject("files", repositoryService.createProcessDefinitionQuery().latestVersion().orderByProcessDefinitionName().asc().list());
		return modelAndView;
	}
	
	@ActionMapping("uploadFile")
    public void uploadFile(@RequestParam("file") MultipartFile file, ActionRequest actionRequest) {
    	if (!file.isEmpty()) {
            try {
            	String name = file.getOriginalFilename();
				EmbeddedProcessApplication embeddedProcessApplication = new EmbeddedProcessApplication();
				ProcessApplicationReference processApplicationReference = new EmbeddedProcessApplicationReferenceImpl(embeddedProcessApplication);
				repositoryService.createDeployment(processApplicationReference).name("Upload Deployment").addInputStream(name, file.getInputStream()).deploy().getId();
				String message = applicationContext.getMessage("editUploaded", new Object[] {name}, actionRequest.getLocale());
				actionRequest.setAttribute("message", message);
            } catch (Exception e) {
				String message = applicationContext.getMessage("editUploadFailed", new Object[] {e.getMessage()}, actionRequest.getLocale());
            	actionRequest.setAttribute("message", message);
            }
        } else {
			String message = applicationContext.getMessage("editUploadEmpty", new Object[] {}, actionRequest.getLocale());
        	actionRequest.setAttribute("message", message);
        }
    }
	
	@ActionMapping("deleteFile")
	public void deleteFile(@RequestParam("id") String id, ActionRequest actionRequest) {
		repositoryService.deleteDeployment(id, true);
		String message = applicationContext.getMessage("editDeleted", new Object[] {id}, actionRequest.getLocale());
        actionRequest.setAttribute("message", message);
	}
}
