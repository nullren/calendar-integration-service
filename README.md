# calendar-integration-service

The best documentation is going to be the openapi schema in `/src/main/resources/api.yaml`.

## Get API token

The API token is linked to your account. To create, first set up oauth credentials

`open $(curl -X POST https://shrouded-fjord-98511.herokuapp.com/authorize | jq -r .googleOauthUri)`

Load the URI it provides in the browser so you can log in to your Google account.

Once allowed, copy the resulting `accessToken` to use as your `Authorization` `Bearer` token in requests.

## Listing events

To view today's events it is just `curl -H 'Authorization: Bearer your-uuid-token' https://shrouded-fjord-98511.herokuapp.com/event/list`

The `start` and `end` query parameters take a date in ISO format (eg, YYYY-MM-DD). Events will be returned between those dates (inclusive). If there are many results, a `nextToken` field will be present to allow pagination. There is also a `timezone` query string parameter that will filter and list events in the chose time zone.

## Updating events

Copying one of the events from the API above, you can make changes and `PUT` to `/event` to update it.

## Postman

Importing `api.yaml` will help set up all the endpoints. It helped me for testing.
