<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Erreur - Carburants</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<main class="login-box">
  <h1>Une erreur est survenue</h1>
  <%-- Only a generic message: never the exception, the stack trace or the server details. --%>
  <p class="msg error" role="alert">La page demandée est introuvable, interdite ou a rencontré un problème.</p>
  <p><a class="btn" href="${pageContext.request.contextPath}/home">Retour à l'accueil</a></p>
</main>
</body>
</html>
