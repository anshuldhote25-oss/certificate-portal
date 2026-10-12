<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Certificates"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<h1>Certificates</h1>

<form method="get" action="${ctx}/certificates" class="search">
  <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Search name, number or course"
         aria-label="Search" data-testid="search-input">
  <select name="status" aria-label="Status" data-testid="status-filter">
    <option value="">All statuses</option>
    <c:forEach var="s" items="${statuses}">
      <option value="${s}" ${selectedStatus == s.name() ? 'selected' : ''}><c:out value="${s.label}"/></option>
    </c:forEach>
  </select>
  <button type="submit" class="primary" data-testid="search-submit">Search</button>
  <a href="${ctx}/certificates" class="button" data-testid="search-clear">Clear</a>
</form>

<p class="muted" data-testid="result-count"><c:out value="${certificates.size()}"/> record(s) found</p>

<c:choose>
  <c:when test="${empty certificates}">
    <div class="card empty" data-testid="empty-state">No certificates match your search.</div>
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
