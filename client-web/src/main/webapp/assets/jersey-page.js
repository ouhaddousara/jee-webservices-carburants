// Part 2, client 4 of the subject: a jQuery page calling the Jersey service.
// - the API lives on the same origin (same host and port), so the browser needs no CORS configuration;
// - the JWT is kept in a JavaScript variable only (never in localStorage, never printed);
// - every value coming from the server is inserted with .text(), never as HTML.
$(function () {
  'use strict';

  var API = '/carburants-jersey/api';
  var token = null;

  function showMessage(kind, text) {
    $('#message').removeClass('ok error info').addClass(kind).text(text).prop('hidden', false);
  }

  function errorText(xhr) {
    if (xhr.responseJSON && xhr.responseJSON.message) {
      return xhr.responseJSON.message;
    }
    if (xhr.status === 0) {
      return "Le service Jersey est injoignable. Vérifiez qu'il est déployé et démarré.";
    }
    return 'Erreur HTTP ' + xhr.status + '.';
  }

  // Default date: today, in the browser's local calendar.
  var now = new Date();
  var pad = function (n) { return (n < 10 ? '0' : '') + n; };
  $('#date').val(now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate()));

  $('#login-form').on('submit', function (event) {
    event.preventDefault();
    $.ajax({
      url: API + '/auth/login',
      method: 'POST',
      contentType: 'application/json',
      dataType: 'json',
      data: JSON.stringify({ username: $('#username').val(), password: $('#password').val() })
    }).done(function (data) {
      token = data.token;
      $('#password').val('');
      $('#prix-button').prop('disabled', false);
      $('#token-state').text('Connecté. Le jeton expire dans ' + data.expiresInSeconds + ' secondes.');
      showMessage('ok', 'Connexion réussie.');
    }).fail(function (xhr) {
      token = null;
      $('#prix-button').prop('disabled', true);
      showMessage('error', errorText(xhr));
    });
  });

  $('#prix-form').on('submit', function (event) {
    event.preventDefault();
    if (!token) {
      showMessage('error', "Connectez-vous d'abord.");
      return;
    }
    $.ajax({
      url: API + '/prix',
      method: 'GET',
      dataType: 'json',
      data: { station: $('#station').val(), date: $('#date').val() },
      headers: { Authorization: 'Bearer ' + token }
    }).done(function (data) {
      $('#message').prop('hidden', true);
      $('#result-title').text(data.station + ' au ' + data.date);
      var body = $('#result-body').empty();
      if (!data.prix || data.prix.length === 0) {
        body.append($('<tr>').append($('<td colspan="3">').text(data.message || 'Aucun prix.')));
      } else {
        $.each(data.prix, function (_, p) {
          body.append($('<tr>')
            .append($('<td>').text(p.carburant))
            .append($('<td class="num">').text(Number(p.prix).toFixed(2)))
            .append($('<td>').text(p.datePrix)));
        });
      }
      $('#result').prop('hidden', false);
    }).fail(function (xhr) {
      $('#result').prop('hidden', true);
      if (xhr.status === 401) {
        token = null;
        $('#prix-button').prop('disabled', true);
        $('#token-state').text('Session expirée : reconnectez-vous.');
      }
      showMessage('error', errorText(xhr));
    });
  });
});
