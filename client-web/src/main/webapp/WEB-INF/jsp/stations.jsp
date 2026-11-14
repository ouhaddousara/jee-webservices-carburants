<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/_header.jsp"><jsp:param name="title" value="Stations"/></jsp:include>

<h1>Stations</h1>

<form method="post" action="${pageContext.request.contextPath}/stations" class="card">
  <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
  <c:choose>
    <c:when test="${not empty edition}">
      <h2>Modifier la station n° <c:out value="${edition.id}"/></h2>
      <input type="hidden" name="action" value="update">
      <input type="hidden" name="id" value="<c:out value='${edition.id}'/>">
    </c:when>
    <c:otherwise>
      <h2>Nouvelle station</h2>
      <input type="hidden" name="action" value="create">
    </c:otherwise>
  </c:choose>
  <div class="grid">
    <div><label for="nom">Nom</label><input type="text" id="nom" name="nom" required maxlength="100" value="<c:out value='${edition.nom}'/>"></div>
    <div><label for="ville">Ville</label><input type="text" id="ville" name="ville" required maxlength="100" value="<c:out value='${edition.ville}'/>"></div>
    <div><label for="adresse">Adresse</label><input type="text" id="adresse" name="adresse" required maxlength="255" value="<c:out value='${edition.adresse}'/>"></div>
  </div>
  <p>
    <button type="submit"><c:out value="${empty edition ? 'Créer' : 'Enregistrer'}"/></button>
    <c:if test="${not empty edition}"><a class="btn secondary" href="${pageContext.request.contextPath}/stations">Annuler</a></c:if>
  </p>
</form>

<table>
  <thead><tr><th class="num">Id</th><th>Nom</th><th>Ville</th><th>Adresse</th><th>Actions</th></tr></thead>
  <tbody>
  <c:forEach var="s" items="${stations}">
    <tr>
      <td class="num"><c:out value="${s.id}"/></td>
      <td><c:out value="${s.nom}"/></td>
      <td><c:out value="${s.ville}"/></td>
      <td><c:out value="${s.adresse}"/></td>
      <td>
        <a class="btn secondary" href="${pageContext.request.contextPath}/stations?edit=<c:out value='${s.id}'/>">Modifier</a>
        <form class="inline" method="post" action="${pageContext.request.contextPath}/stations"
              data-confirm="Supprimer cette station ?">
          <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
          <input type="hidden" name="action" value="delete">
          <input type="hidden" name="id" value="<c:out value='${s.id}'/>">
          <button type="submit" class="danger">Supprimer</button>
        </form>
      </td>
    </tr>
  </c:forEach>
  <c:if test="${empty stations}"><tr><td colspan="5" class="muted">Aucune station.</td></tr></c:if>
  </tbody>
</table>

<%@ include file="/WEB-INF/jsp/_footer.jspf" %>
