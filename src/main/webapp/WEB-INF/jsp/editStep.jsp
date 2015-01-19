<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
    import="org.camunda.bpm.engine.form.FormField,
	java.util.Map"
%><%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui"%>
<portlet:defineObjects />

<portlet:actionURL windowState="normal" name="commitTask" var="confirm" />
<aui:script use="aui-base,aui-dialog,aui-io,liferay-portlet-url"></aui:script>
<c:choose>
	<c:when test="${loggedin == false}">
		<spring:message code="notLoggedIn" />
	</c:when>
	<c:otherwise>
		<aui:form method="POST" action="${confirm}">
			<input type="hidden" name="<%=renderResponse.getNamespace()%>taskId" value="${taskId}" />
			<input type="hidden" name="<%=renderResponse.getNamespace()%>processDefinitionId" value="${processDefinitionId}" />
			<input type="hidden" name="<%=renderResponse.getNamespace()%>userId" value="${userId}" />
			<table class="table table-bordered table-hover table-striped">
				<tr>
					<th><spring:message code="taskVariable" text="Name" /></th>
					<th><spring:message code="taskVariableValue" text="Value" /></th>
				</tr>
				<c:forEach items="${formFields}" var="field">
					<tr>
						<td colspan="2">
							<c:choose>
								<c:when test="${field.typeName eq 'boolean'}">
								<aui:input type="checkbox" label="${field.label}" name="${field.id}" value="${taskVars[field.id] != null ? taskVars[field.id] : field.defaultValue}"></aui:input>
								</c:when>

								<c:when test="${field.typeName eq 'long'}">
									<aui:input name="${field.id}" label="${field.label}" value="${taskVars[field.id] != null ? taskVars[field.id] : field.defaultValue}">
										<aui:validator name="max"><%=Long.MAX_VALUE%></aui:validator>
									</aui:input>
								</c:when>
								
								<c:otherwise>
									<aui:input type="text" label="${field.label}" name="${field.id}" value="${taskVars[field.id] != null ? taskVars[field.id] : field.defaultValue}"></aui:input>
								</c:otherwise>
					    	</c:choose>
						</td>
					</tr>
				</c:forEach>

				<c:forEach items="${taskVarsNotForEdit}" var="taskVar">
					<tr>
						<td>${taskVar.key}</td>
						<td>${taskVar.value}</td>
					</tr>
				</c:forEach>

			</table>
			<spring:message code="editTaskButton" text="Complete" var="lblCommit" />
			<aui:button type="submit" value="${lblCommit}" />
		</aui:form>
	</c:otherwise>
</c:choose>