<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/_header.jsp"><jsp:param name="title" value="Carburants"/></jsp:include>

<h1>Carburants</h1>

<form method="post" action="${pageContext.request.contextPath}/carburants" class="card">
  <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
  <c:choose>
    <c:when test="${not empty edition}">
      <h2>Modifier le carburant n° <c:out value="${edition.id}"/></h2>
      <input type="hidden" name="action" value="update">
      <input type="hidden" name="id" value="<c:out value='${edition.id}'/>">
    </c:when>
    <c:otherwise>
      <h2>Nouveau carburant</h2>
      <input type="hidden" name="action" value="create">
    </c:otherwise>
  </c:choose>
  <div class="grid">
    <div><label for="nom">Nom</label><input type="text" id="nom" name="nom" required maxlength="100" value="<c:out value='${edition.nom}'/>"></div>
    <div><label for="description">Description</label><input type="text" id="description" name="description" maxlength="255" value="<c:out value='${edition.description}'/>"></div>
  </div>
  <p>
    <button type="submit"><c:out value="${empty edition ? 'Créer' : 'Enregistrer'}"/></button>
    <c:if test="${not empty edition}"><a class="btn secondary" href="${pageContext.request.contextPath}/carburants">Annuler</a></c:if>
  </p>
</form>

<table>
  <thead><tr><th class="num">Id</th><th>Nom</th><th>Description</th><th>Actions</th></tr></thead>
  <tbody>
  <c:forEach var="c" items="${carburants}">
    <tr>
      <td class="num"><c:out value="${c.id}"/></td>
      <td><c:out value="${c.nom}"/></td>
      <td><c:out value="${c.description}"/></td>
      <td>
        <a class="btn secondary" href="${pageContext.request.contextPath}/carburants?edit=<c:out value='${c.id}'/>">Modifier</a>
        <form class="inline" method="post" action="${pageContext.request.contextPath}/carburants"
              data-confirm="Supprimer ce carburant ?">
          <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
          <input type="hidden" name="action" value="delete">
          <input type="hidden" name="id" value="<c:out value='${c.id}'/>">
          <button type="submit" class="danger">Supprimer</button>
        </form>
      </td>
    </tr>
  </c:forEach>
  <c:if test="${empty carburants}"><tr><td colspan="4" class="muted">Aucun carburant.</td></tr></c:if>
  </tbody>
</table>

<%@ include file="/WEB-INF/jsp/_footer.jspf" %>
