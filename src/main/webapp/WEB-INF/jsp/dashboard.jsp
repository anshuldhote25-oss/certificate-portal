<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<h1>Dashboard</h1>
<section class="cards">
  <div class="stat total">
    <span class="num" data-testid="count-total"><c:out value="${total}"/></span>
    <span class="lbl">Total certificates</span>
  </div>
  <c:forEach var="e" items="${counts}">
    <a class="stat" href="${ctx}/certificates?status=${e.key}">
      <span class="num" data-testid="count-${e.key}"><c:out value="${e.value}"/></span>
      <span class="lbl"><c:out value="${e.key.label}"/></span>
    </a>
  </c:forEach>
</section>

<section class="card">
  <h2>Recently updated</h2>
  <table data-testid="recent-table">
    <thead><tr><th>Number</th><th>Recipient</th><th>Course</th><th>Status</th></tr></thead>
    <tbody>
    <c:forEach var="c" items="${recent}">
      <tr>
        <td><a href="${ctx}/certificates/${c.id}"><c:out value="${c.certNo}"/></a></td>
        <td><c:out value="${c.recipientName}"/></td>
        <td><c:out value="${c.course}"/></td>
        <td><span class="badge ${c.status.css}"><c:out value="${c.status.label}"/></span></td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</section>
<%@ include file="/WEB-INF/jsp/footer.jspf" %>
