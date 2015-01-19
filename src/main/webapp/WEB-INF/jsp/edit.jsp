<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<portlet:defineObjects />
<portlet:actionURL name="uploadFile" var="uploadFile" />

<c:if test="${message ne null}">
	<div class="alert">${message}</div>
	<hr />
</c:if>
<table class="table table-bordered table-hover table-striped">
	<tr>
		<th><spring:message code="processId" /></th>
		<th><spring:message code="processName" /></th>
		<th><spring:message code="taskDescription" /></th>
		<th />
	</tr>
	<c:forEach items="${files}" var="file">
	<tr>
		<td>${file.id}</td>
		<td>${file.name}</td>
		<td>${file.description}</td>
		<td>
			<a href="<portlet:actionURL name="deleteFile"><portlet:param name="id" value="${file.deploymentId}"/></portlet:actionURL>">
				delete
			</a>
		</td>
	</tr>
	</c:forEach>
</table>
<hr />
<form method="POST" enctype="multipart/form-data" action="${uploadFile}">
		<spring:message code="editUploadLabel" />:<br />
		<input type="file" name="file"><br />
		<input type="submit" value="Upload">
</form>