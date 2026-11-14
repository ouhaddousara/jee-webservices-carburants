<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${param.title}"/> - Carburants</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
  <script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</head>
<body>
<header class="bar">
  <span class="brand">Carburants</span>
  <nav aria-label="Navigation principale">
    <a href="${pageContext.request.contextPath}/home">Accueil</a>
    <a href="${pageContext.request.contextPath}/stations">Stations</a>
    <a href="${pageContext.request.contextPath}/carburants">Carburants</a>
    <a href="${pageContext.request.contextPath}/prix">Prix</a>
    <a href="${pageContext.request.contextPath}/consultation">Consultation</a>
    <a href="${pageContext.request.contextPath}/jersey.html">Page jQuery (Jersey)</a>
  </nav>
  <form method="post" action="${pageContext.request.contextPath}/logout">
    <input type="hidden" name="_csrf" value="<c:out value='${csrf}'/>">
    <span class="who"><c:out value="${user}"/></span>
    <button type="submit" class="secondary">Se déconnecter</button>
  </form>
</header>
<main>
<c:if test="${not empty flashOk}"><p class="msg ok" role="status"><c:out value="${flashOk}"/></p></c:if>
<c:if test="${not empty flashError}"><p class="msg error" role="alert"><c:out value="${flashError}"/></p></c:if>
