# Enlighten login

```
$ curl -vvv -X POST https://enlighten.enphaseenergy.com/login/login.json? \
            -F "user[email]=bart@cloutrix.com"                            \
            -F 'user[password]=BKi********5%h'

{ "message":"success",
  "session_id":"5ae67aa246e6947c0a87bb22f17d75ad",
  "manager_token":"eyJhbGciOiJIUzI1NiJ9.eyJkYXRhIjp7InNlc3Npb25faWQiOiI1YWU2N2FhMjQ2ZTY5NDdjMGE4N2JiMjJmMTdkNzVhZCIsImNvbXBhbnlfaWQiOm51bGwsImVtYWlsX2lkIjoiYmFydEBjbG91dHJpeC5jb20iLCJ1c2VyX2lkIjoyMzQ3ODI3LCJjbGllbnRfYXBwIjoiaXRrMyIsImZpcnN0X25hbWUiOiJCYXJ0IiwibGFzdF9uYW1lIjoiVmVyY2FtbWVuIiwibG9naW5fdXNlciI6bnVsbCwiaXNfZGlzdHJpYnV0b3IiOmZhbHNlfSwiZXhwIjoxNjg5NjE4NTY2LCJzdWIiOiJiYXJ0QGNsb3V0cml4LmNvbSJ9.4ASKGp8yzXWEREJ3GTUlsggwqzB7RuR7KSU1oc0ht5Y",
  "is_consumer":true
}

// manager-token JWT:
{
  "data": {
    "session_id": "5ae67aa246e6947c0a87bb22f17d75ad",
    "company_id": null,
    "email_id": "bart@cloutrix.com",
    "user_id": 2347827,
    "client_app": "itk3",
    "first_name": "Bart",
    "last_name": "Vercammen",
    "login_user": null,
    "is_distributor": false
  },
  "exp": 1689618566,
  "sub": "bart@cloutrix.com"
}
```
```
$ curl -X POST https://entrez.enphaseenergy.com/tokens  \
       -H "Content-Type: application/json"              \
       -d "{\"session_id\": \"$session_id\", \"serial_num\": \"$envoy_serial\", \"username\": \"$user\"}"

{ "generation_time":1688975561,
  "token":"eyJraWQiOiI3ZDEwMDA1ZC03ODk5LTRkMGQtYmNiNC0yNDRmOThlZTE1NmIiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiIxMjIwMzgwMTUyNzMiLCJpc3MiOiJFbnRyZXoiLCJlbnBoYXNlVXNlciI6Im93bmVyIiwiZXhwIjoxNzIwNTExNTYxLCJpYXQiOjE2ODg5NzU1NjEsImp0aSI6IjJkYmNkY2NiLWNkZWQtNGFjYi1iYjExLTQzYjg0ZjkzNjE1NCIsInVzZXJuYW1lIjoiYmFydEBjbG91dHJpeC5jb20ifQ.X6E-MKly5o0ugVsHKzlfUqk4BVVX9IXQ7VZSzV_hDrDyupl0EN3-rLXb7JT68bpUlDmrQTXMHSg1evGn1_Hn0A",
  "expires_at":1720511561
}

// JWT token:
{
  "kid": "7d10005d-7899-4d0d-bcb4-244f98ee156b",
  "typ": "JWT",
  "alg": "ES256"
}
{
  "aud": "122038015273",
  "iss": "Entrez",
  "enphaseUser": "owner",
  "exp": 1720511561,
  "iat": 1688975561,
  "jti": "2dbcdccb-cded-4acb-bb11-43b84f936154",
  "username": "bart@cloutrix.com"
}
```
