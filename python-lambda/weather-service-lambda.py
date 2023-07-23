import urllib3
import json
import os

def lambda_handler(event, context):

    http = urllib3.PoolManager()
    appId = appId = os.environ['APPID']
    requestParams = event.get('rawQueryString')
    units = event.get('queryStringParameters')['units']
    currentWeatherUrl = 'http://api.openweathermap.org/data/2.5/weather?APPID={}&{}'.format(appId,requestParams)
    currWeatherRequest = http.request('GET', currentWeatherUrl)
    response = {}

    if currWeatherRequest.status == 200:
        responseBody = {}
        responseBody['current'] = json.loads(currWeatherRequest.data)
        latitude = responseBody.get('current')['coord']['lat']
        longitude = responseBody.get('current')['coord']['lon']

        weatherForecastUrl = 'http://api.openweathermap.org/data/2.5/onecall?APPID={}&lat={}&lon={}&units={}'.format(appId,latitude,longitude,units)
        forecastRequest = http.request('GET', weatherForecastUrl)

        if forecastRequest.status == 200:
            responseBody['forecast'] = json.loads(forecastRequest.data)
            response = {
                "statusCode": forecastRequest.status,
                "headers": {
                    "Access-Control-Allow-Origin": "*",
                    "Content-type" :"application/json"
                },
                "body": json.dumps(responseBody)
            }
        else:
            response = {
                "statusCode": forecastRequest.status,
                "headers": {
                    "Access-Control-Allow-Origin": "*",
                    "Content-type" :"application/json"
                }
            }
    else:
        response = {
            "statusCode": currWeatherRequest.status,
            "headers": {
                "Access-Control-Allow-Origin": "*",
                "Content-type" :"application/json"
            }
        }

    return response