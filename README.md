# restTester

TODO:
- добавить Expected Result к запросу, чтобы можно было проверить данные в ответе. 
  Есть разница ответ получен и верен по схеме и в ответ получен нужный результат
- добавить notExistAction в scenario.repeatebleWhile
- переименовать repeatableWhile в repeatWhile
- в properties добавить поле resultLogFile куда будет логироваться только результат
- добавить несколько логеров с разными уровнями записи.
    По итогу хочу получить такую структуру файлов
    /dd-MM-yyyy/hh-mm-ss/info.log - логи INFO
    /dd-MM-yyyy/hh-mm-ss/debug.log - логи DEBUG
    /dd-MM-yyyy/hh-mm-ss/result.log  - логи результата