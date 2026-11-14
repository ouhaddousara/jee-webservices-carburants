<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Connexion - Carburants</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<main class="login-box">
  <h1>Connexion</h1>
  <c:if test="${not empty flashError}"><p class="msg error" role="alert"><c:out value="${flashError}"/></p></c:if>
  <form method="post" action="${pageContext.request.contextPath}/login" class="card" autocomplete="off">
    <p>
      <label for="username">Identifiant</label>
      <input type="text" id="username" name="username" required maxlength="100" autofocus>
    </p>
    <p>
      <label for="password">Mot de passe</label>
      <input type="password" id="password" name="password" required maxlength="200">
    </p>
    <button type="submit">Se connecter</button>
    <p class="muted">Les identifiants sont vérifiés par le service SOAP.</p>
  </form>
</main>
</body>
</html>
