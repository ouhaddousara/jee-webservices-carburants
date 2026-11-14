<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/_header.jsp"><jsp:param name="title" value="Accueil"/></jsp:include>

<h1>Gestion des carburants</h1>

<div class="tiles">
  <div class="tile"><strong><c:out value="${empty nbStations ? '-' : nbStations}"/></strong>stations</div>
  <div class="tile"><strong><c:out value="${empty nbCarburants ? '-' : nbCarburants}"/></strong>types de carburant</div>
</div>

<div class="card">
  <p>Cette application appelle le service <strong>JAX-WS</strong> (SOAP) pour toutes ses opérations.</p>
  <ul>
    <li><a href="${pageContext.request.contextPath}/stations">Stations</a> : créer, modifier, supprimer.</li>
    <li><a href="${pageContext.request.contextPath}/carburants">Carburants</a> : créer, modifier, supprimer.</li>
    <li><a href="${pageContext.request.contextPath}/prix">Prix journaliers</a> : enregistrer et corriger le prix d'un carburant dans une station.</li>
    <li><a href="${pageContext.request.contextPath}/consultation">Consultation</a> : prix des carburants selon une station et une date.</li>
    <li><a href="${pageContext.request.contextPath}/jersey.html">Page jQuery</a> : même consultation, via le service Jersey.</li>
  </ul>
</div>

<%@ include file="/WEB-INF/jsp/_footer.jspf" %>
