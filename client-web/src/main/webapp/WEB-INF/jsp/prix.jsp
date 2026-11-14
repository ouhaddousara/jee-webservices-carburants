<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/_header.jsp"><jsp:param name="title" value="Prix"/></jsp:include>

<h1>Prix journaliers</h1>

<form method="get" action="${pageContext.request.contextPath}/prix" class="card">
  <h2>Choisir une station</h2>
  <div class="grid">
    <div>
      <label for="stationId">Station</label>
      <select id="stationId" name="stationId" required>
        <option value="">- choisir -</option>
        <c:forEach var="s" items="${stations}">
          <option value="<c:out value='${s.id}'/>" ${s.id == stationId ? 'selected' : ''}><c:out value="${s.nom}"/> (<c:out value="${s.ville}"/>)</option>
        </c:forEach>
      </select>
    </div>
    <div><button type="submit">Afficher l'historique</button></div>
  </div>
</form>

<c:if test="${not empty stationId}">
  <form method="post" action="${pageContext.request.contextPath}/prix" class="card">
    <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
    <input type="hidden" name="action" value="create">
    <input type="hidden" name="stationId" value="<c:out value='${stationId}'/>">
    <h2>Enregistrer un prix</h2>
    <div class="grid">
      <div>
        <label for="carburantId">Carburant</label>
        <select id="carburantId" name="carburantId" required>
          <c:forEach var="c" items="${carburants}">
            <option value="<c:out value='${c.id}'/>"><c:out value="${c.nom}"/></option>
          </c:forEach>
        </select>
      </div>
      <div><label for="date">Date</label><input type="date" id="date" name="date" required></div>
      <div><label for="prix">Prix (MAD/L)</label><input type="text" id="prix" name="prix" required inputmode="decimal" placeholder="10.45"></div>
      <div><button type="submit">Enregistrer</button></div>
    </div>
  </form>

  <h2>Historique</h2>
  <table>
    <thead><tr><th>Date</th><th>Carburant</th><th class="num">Prix (MAD/L)</th><th>Actions</th></tr></thead>
    <tbody>
    <c:forEach var="h" items="${historique}">
      <tr>
        <td colspan="4">
          <form class="inline" method="post" action="${pageContext.request.contextPath}/prix">
            <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<c:out value='${h.id}'/>">
            <input type="hidden" name="stationId" value="<c:out value='${stationId}'/>">
            <input type="date" name="date" value="<c:out value='${h.date}'/>" required aria-label="Date">
            <strong><c:out value="${h.carburantNom}"/></strong>
            <input type="text" name="prix" value="<c:out value='${h.prix}'/>" required inputmode="decimal" aria-label="Prix">
            <button type="submit" class="secondary">Modifier</button>
          </form>
          <form class="inline" method="post" action="${pageContext.request.contextPath}/prix" data-confirm="Supprimer ce prix ?">
            <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="id" value="<c:out value='${h.id}'/>">
            <input type="hidden" name="stationId" value="<c:out value='${stationId}'/>">
            <button type="submit" class="danger">Supprimer</button>
          </form>
        </td>
      </tr>
    </c:forEach>
    <c:if test="${empty historique}"><tr><td colspan="4" class="muted">Aucun prix enregistré pour cette station.</td></tr></c:if>
    </tbody>
  </table>
</c:if>

<%@ include file="/WEB-INF/jsp/_footer.jspf" %>
