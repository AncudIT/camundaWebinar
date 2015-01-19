<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<portlet:defineObjects/>
<portlet:actionURL name="newOrder" var="newOrder"/>
<portlet:actionURL name="confirmOrder" var="confirmOrder"/>

<spring:message var="editTaskTitle" code="editTaskTitle"/>
<spring:message var="showTaskTitle" code="showTaskTitle"/>
<spring:message var="taskEditButton" code="taskEditButton"/>
<spring:message var="taskShowButton" code="taskShowButton"/>
<spring:message code="newProcess" var="newProcess"/>
<c:choose>
    <c:when test="${loggedin == false}">
        <spring:message code="notLoggedIn"/>
    </c:when>
    <c:otherwise>
        <c:if test="${message ne null}">
            <div class="alert">${message}</div>
            <hr />
        </c:if>
        <h3><spring:message code="availableTasks"/></h3>
        <table class="table table-bordered table-hover table-striped">
            <tr>
                <th><spring:message code="processId" text="Process Id"/></th>
                <th><spring:message code="taskName"/></th>
                <th><spring:message code="taskDescription"/></th>
                <th><spring:message code="taskAction"/></th>
            </tr>
            <c:forEach items="${tasks}" var="task">
                <tr>
                    <td>${task.processInstanceId}</td>
                    <td>${task.name}</td>
                    <td>${task.description}</td>
                    <td>
                        <aui:button value="${taskEditButton}" name="openStepProcessor" cssClass="taskButton" data-id="${task.id}"/>
                        <aui:button value="${taskShowButton}" name="showStep" cssClass="showTaskButton" data-id="${task.id}" data-step="${task.getTaskDefinitionKey()}"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${files.size() > 0}">
            <hr/>
            <form action="${newOrder}" method="post">
                <select name="<portlet:namespace />processDefinitionId" id="processselect">
                    <option />
                    <c:forEach items="${files}" var="filesfile">
                        <option value="${filesfile.id}">${filesfile.name}</option>
                    </c:forEach>
                </select>
                <aui:button id="newProcessButton" value="${newProcess}" />
            </form>
        </c:if>

    </c:otherwise>
</c:choose>
<script type="text/javascript">
    var liferayCamundaWorkflowPortletId = '<%=request.getAttribute("PORTLET_ID")%>';
</script>
