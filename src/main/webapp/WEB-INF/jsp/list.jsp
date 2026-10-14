<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Certificates"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<h1>Certificates</h1>

<p class="muted" data-testid="result-count"><c:out value="${certificates.size()}"/> record(s) found</p>

<c:choose>
  <c:when test="${empty certificates}">
    <div class="card empty" data-testid="empty-state">No certificates yet.</div>
  </c:when>
  <c:otherwise>
    <table data-testid="cert-table">
      <thead><tr><th>Number</th><th>Recipient</th><th>Course</th><th>Issue date</th><th>Status</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="c" items="${certificates}">
        <tr data-testid="cert-row">
          <td data-testid="row-number"><c:out value="${c.certNo}"/></td>
          <td data-testid="row-name"><c:out value="${c.recipientName}"/></td>
          <td><c:out value="${c.course}"/></td>
          <td><c:out value="${c.issueDate}"/></td>
          <td><span class="badge ${c.status.css}" data-testid="row-status"><c:out value="${c.status.label}"/></span></td>
          <td><a href="${ctx}/certificates/${c.id}" data-testid="row-view">View</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/jsp/footer.jspf" %>
