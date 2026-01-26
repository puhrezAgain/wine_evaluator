# 🍷 Wine Evaluator

Understand wine lists in the wild.
Wine Evaluator analyzes wine lists to learn real-world prices and lets
users instantly check whether a specific wine is fairly priced.


## App Flow

Two main use cases:
1. List Check: Analyze an entire menu and get analysis on it.
2. Wine Check: Analyze a single wine.


### List Check
```
Consumer App
   │
   │ Check wine list (PDF / Image)
   ▼
Wine Evaluator
   │
   │ Async analysis
   │ • parse menu
   │ • extract wines & prices
   │ • add information to known prices
   │ • compare with known prices
   ▼
Consumer App
   │
   │ Wine list evaluation
   │ • fair / overpriced / deal
   │ • reference prices
```


### Wine Check

```
Consumer App
   │
   │ Check wine + price
   ▼
Wine Evaluator
   │
   │ Match against known prices
   ▼
Consumer App
   │
   │ Immediate result
   │ • reference price
   │ • % over / under
```

## REST API

Wine Evaluator supports two checks:

🍽️ Wine List Check — async, document-based

🍷 Single Wine Check — sync, query-based

### 🍽️ Wine List Check (Async)

Check an entire wine list (PDF or image).

`POST /analysis`

Upload a wine list and start analysis.
```
curl -X POST /analysis \
  -F "file=@winelist.pdf"
```

Response — 202 Accepted
```
{
  "record": {
    "id": "analysis-id",
    "status": "PENDING"
  }
}
```
`GET /analysis/{id}`

Poll for results.

* `202 Accepted` → still processing

* `200 OK` → analysis complete

* `422 Unprocessable` → analysis failed

Done response
```
{
  "id": "analysis-id",
  "results": [
    {
      "price": 48,
      "referencePrice": 32,
      "deltaPercent": 50.0
    }
  ]
}
```
### 🍷 Single Wine Check (Sync)

Instantly check one wine price.

`POST /analysis`

Send wine name and menu price.
```
curl -X POST /analysis \
  -H "Content-Type: application/json" \
  -d '{"wine":"Viña Tondonia Reserva 2011","price":48}'
```

Response — `200 OK`
```
{
  "original": "Viña Tondonia Reserva 2011",
  "queryPrice": 48,
  "matches": [
    {
      "referencePrice": 32,
      "deltaPercent": 50.0
    }
  ]
}
```
Summary
```
POST /analysis (file) → check wine list (async)
POST /analysis (json) → check single wine (sync)
GET  /analysis/{id}   → retrieve wine list results
```
Notes

* Wine list checks return results for that list
* Observed prices are stored and reused
* Single wine checks are read-only and immediate