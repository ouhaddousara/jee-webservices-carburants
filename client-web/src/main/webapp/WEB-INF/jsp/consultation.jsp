<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/_header.jsp"><jsp:param name="title" value="Consultation"/></jsp:include>

<h1>Prix des carburants selon la station et la date</h1>
<p class="muted">Appel de la méthode <code>prixCarburants(nom_station, date)</code> du service JAX-WS.</p>

<form method="get" action="${pageContext.request.contextPath}/consultation" class="card">
  <div class="grid">
    <div>
      <label for="station">Station</label>
      <select id="station" name="station" required>
        <option value="">- choisir -</option>
        <c:forEach var="s" items="${stations}">
          <option value="<c:out value='${s.nom}'/>" ${s.nom == station ? 'selected' : ''}><c:out value="${s.nom}"/></option>
        </c:forEach>
      </select>
    </div>
    <div><label for="date">Date</label><input type="date" id="date" name="date" required value="<c:out value='${date}'/>"></div>
    <div><button type="submit">Consulter</button></div>
  </div>
</form>

<c:if test="${not empty resultat}">
  <h2><c:out value="${resultat.station}"/> au <c:out value="${resultat.date}"/></h2>
  <c:choose>
    <c:when test="${empty resultat.prix}">
      <p class="msg info"><c:out value="${resultat.message}"/></p>
    </c:when>
    <c:otherwise>
      <table>
        <thead><tr><th>Carburant</th><th class="num">Prix (MAD/L)</th><th>Prix fixé le</th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${resultat.prix}">
          <tr>
            <td><c:out value="${p.carburant}"/></td>
            <td class="num"><c:out value="${p.prix}"/></td>
            <td><c:out value="${p.datePrix}"/></td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</c:if>

<%@ include file="/WEB-INF/jsp/_footer.jspf" %>
