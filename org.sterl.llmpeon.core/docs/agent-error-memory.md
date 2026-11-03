---
idPrefix: AEM
---

# API-Fehler ins Agent-Memory

> **Status:** ✅ done (2026-09-30, Paul; Delta nach Da-Dok-REJECTED). **Anlass:** Turn stirbt still an API-Fehlern
> (400 `exceed_context_size_error`) — die History enthält den Abbruch
> nicht; der Agent (und Paul) wissen beim nächsten Turn nicht, warum Schluss war. Verwandt:
> [agents-retry.md](agents-retry.md) (ApiRetry — erst nach erschöpftem Retry),
> [compact.md](compact.md) R-CC-7 (Fehlerklassen-Tabelle, ein Bestand zwei Verbraucher) und
> R-CC-17 (Compact-only, 🔒 Paul: 400-Pfad wird hier NICHT zusätzlich instrumentiert).
> **AiChatView-Wiring = manuelle Verifikation, Paul-Smoke steht aus.**

## Business Rules

### R-AEM-1 — Terminaler Fehler wird zur Memory-Message ✅ done (AiChatView-Wiring manuell)

Fliegt ein Turn mit einer **echten Exception** raus (der `catch`-Pfad in `AiChatView` — nicht der
stille Cancel-Pfad), wird die Fehlermeldung als **kompakte Message ins Agent-Memory** eingefügt:

- **Wann:** nur **terminal** — ApiRetry erschöpft, oder nicht-retryable (Fehlerklassen-Tabelle
  R-CC-7/ApiRetry; `exceed_context_size_error` ist nicht-retryable → sofort). **Nie** bei
  Cancel/Stop (User-Abbruch ist kein Fehlerzustand).
- **Inhalt:** `e.getMessage()` (Provider-Ursache, kompakt — kein Stacktrace, keine Doppelt-Emission
  neben dem bestehenden UI-Problem-Report).
- **Wirkung:** der Agent **sieht** den Fehler im nächsten Turn und kann reagieren (z. B. selbst
  `compactSession` rufen). Grenze (dokumentiert, nicht gelöst): im harten Kontext-Überlauf kann
  trotzdem kein Turn mehr fahren — das Feature macht den Abbruch recoverbar, er heilt ihn nicht.

#### UC-AEM-1 — terminalErrorLandsInMemory ✅

- GIVEN ApiRetry erschöpft (oder nicht-retryable Fehler) WHEN der Turn fehlschlägt THEN das
  Agent-Memory enthält eine kompakte Message mit `e.getMessage()`.

#### UC-AEM-2 — cancelNeverInserts ✅

- GIVEN der User drückt Stop (Cancellation) WHEN der Turn abbricht THEN **keine** Memory-Message.

### R-AEM-2 — Dedup: genau einmal pro Memory ✅ done

Dieselbe Fehler-Message wird **nur einmal** ins Memory eingefügt (`containsMessage`-Muster wie
COMPACT_HINT/Spam-Guard) — aufeinanderfolgende identische Fehler füttern den Kontext
nicht.

#### UC-AEM-3 — duplicateErrorInsertedOnce ✅

- GIVEN der Fehler ist bereits im Memory WHEN ein identischer terminaler Fehler auftritt THEN
  keine zweite Kopie.

### R-AEM-3 — Nutzlose Provider-Fehler-Messages nennen URL, Klasse, Ursache ✅ done (2026-10-29, Da-Dok-Review nach REJECTED-Delta; Trigger-Form nach Messung, Paul-Rückversicherung ⏳)

Ein Model-API-Connect-Fehler ist in der Chat-PROBLEM-Zeile nicht von einem MCP-Connect-Fehler
unterscheidbar — die Wrapper-Schichten (JDK-`CompletableFuture`-Umschlag, langchain4j
`ExceptionMapper` mit cause-only-Konstruktoren) liefern am Choke-Point blank Messages oder bloße
Klassennamen. Am Choke-Point `ToolLoopRequest.call` wird eine `RuntimeException` mit **nutzloser
Message** daher neu geworfen als
`AI call to <effective URL> failed: <Exception-Klasse> — <cause>` (Cause bleibt in der Chain).

**Nutlos ist die Message, wenn** sie blank ist **oder** exakt dem Klassennamen ihres Causes
entspricht (JDK-Envelope: `CompletionException` mit `getMessage() == cause.getClass().getName()`).

- **Nur der AI-Call-Pfad** — Tool-Fehler laufen nicht durch `ToolLoopRequest.call` (siehe
  [agents-retry.md](agents-retry.md)).
- **Cancel-Parität:** `CancellationException` wird **nie** gewrappt — auch nicht, wenn sie nur als
  Cause in der Kette steckt (`ExceptionUtil.isCanceled`, cause-chain-basiert).
- **Inhaltstragende Message bleibt unverändert** — keine Doppel-Anreicherung (z. B.
  Provider-Fehlertexte, `"closed"` beim Mid-Stream-Abbruch).
- **Rendering:** Klassen = einfacher Name; Cause-Message (erste Zeile, getrimmt) wird als
  `<cause>: <message>` angehängt, wenn vorhanden; **fehlt der Cause, entfällt das `—`-Suffix**;
  leere/nicht auflösbare URL → `(no url configured)`.
- **Bewusste Grenze:** Mid-Stream-Abbrüche (`LangChain4jException` mit Message `"closed"`) bleiben
  unverändert — Revisit im Fehlerklassen-Zyklus (R-CC-7). Ebenso **verschachtelte Envelopes**
  (`RuntimeException(CompletionException(…))`): geprüft wird nur die äußere Stufe — eine
  Root-Cause-basierte Anreicherung (auch inhaltstragende Provider-Messages) ist ein eigener
  Entscheid, nicht jetzt.
- **Zweck:** Model-API-Fehler (URL sichtbar) von MCP-Connect-Fehlern unterscheidbar; Diagnose
  ohne Message-Rätselraten.

#### UC-AEM-4 — messageLessExceptionNamesUrlAndClass ✅

- GIVEN eine `RuntimeException` mit blank Message WHEN der AI-Call scheitert THEN die geworfene
  Exception nennt effective URL + Exception-Klasse + Ursache, der Cause bleibt in der Chain.

#### UC-AEM-5 — cancellationIsNeverWrapped ✅

- GIVEN eine `CancellationException` WHEN der Call abbricht THEN sie fliegt unverändert hoch —
  kein Wrap, kein Retry.

#### UC-AEM-6 — exceptionWithMessageUntouched ✅

- GIVEN eine `RuntimeException` mit inhaltstragender Message (nicht blank, nicht
  Cause-Klassenname) WHEN der Call scheitert THEN die Message bleibt unverändert (keine
  Doppel-Anreicherung).

#### UC-AEM-7 — envelopeWithCauseClassNameIsEnriched ✅

- GIVEN die Produktionsschichten werfen eine `CompletionException`-Form mit
  `getMessage() == Cause-Klassenname` (z. B. `ConnectException` ohne Message) WHEN der Call
  scheitert THEN die Exception wird mit URL + Klasse + Ursache angereichert.

### R-AEM-4 — Stream-Truncation bricht den Tool-Loop ab ✅ done (2026-11-03)

Eine kaputte Provider-Antwort (Stream-Truncation, unvollständige Tool-Call-Payload) ist **nicht
retryable** — ein Retry bekommt wieder dieselbe kaputte Antwort, und weiterzuloopen mit einer
halbgeparsten Tool-Call-Payload korrumpiert den Turn. Erkennung und Wirkung:

- **Erkennung** am Choke-Point `ToolLoopRequest.call`: eine Exception, deren Cause-Kette eine
  Stream-Truncation-Signatur enthält — nur die spezifischen Transport-Signaturen
  (`TransferEncodingError`, `Not enough data to satisfy transfer length`, `Response payload is not
  completed`, case-insensitive, über die Cause-Kette), **nicht** der breite Begriff `truncated`
  (matcht sonst auch Token-Limit-Truncation), wird als `ToolExecutionPermanentFailureException` neu
  geworfen.
- **Kein Retry:** `ApiRetry` erhält einen Guard (`e -> !isPermanentCallFailure(e)`), der eine
  Permanent-Failure-Exception und eine Truncation-Signatur **vor** dem Backoff stoppt.
- **Loop-Stopp:** `ToolService.executeLoop` fängt die
  `ToolExecutionPermanentFailureException` und beendet die Schleife sofort — es wird **keine**
  weitere Model-Runde gefahren, kein `response == null`-IllegalState geworfen, die zuletzt gültige
  `response` (oder `null`) zurückgegeben.
- **Cancel-Parität (R-AEM-3):** eine `CancellationException` wird weiterhin **nie** als
  Permanent-Failure gewrappt — sie fliegt unverändert hoch.
- **Nur der AI-Call-Pfad** — Tool-Fehler laufen nicht durch `ToolLoopRequest.call`
  ([agents-retry.md](agents-retry.md)).

#### UC-AEM-8 — streamTruncationAbortsLoop ✅

- GIVEN die Provider-Antwort bricht mit einer Stream-Truncation-Exception ab (Marker in der
  Cause-Kette) WHEN der AI-Call im Tool-Loop scheitert THEN der Loop wird ohne Retry und ohne
  weitere Runde beendet und die `ToolExecutionPermanentFailureException` gemeldet.

#### UC-AEM-9 — cancelNotTreatedAsPermanent ✅

- GIVEN eine `CancellationException` WHEN der Call abbricht THEN sie wird nicht als
  Permanent-Failure gewrappt, sondern fliegt unverändert hoch.

## Verifikation

`AiChatView` = SWT-UI — Automatisierung nur soweit die Memory-Logik im Core testbar ist (Dedup,
Message-Bau); der AiChatView-Wiring wird manuell verifiziert (User-Smoke), wie in UI-Stories
geübtes Muster.
