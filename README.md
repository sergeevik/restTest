[![Build Status](https://travis-ci.org/sergeevik/restTester.svg?branch=master)](https://travis-ci.org/sergeevik/restTester)  [![codecov](https://codecov.io/gh/sergeevik/restTester/branch/master/graph/badge.svg)](https://codecov.io/gh/sergeevik/restTester)

# restTester

TODO:
- [x] добавить Expected Result к запросу, чтобы можно было проверить данные в ответе. 
  Есть разница ответ получен и верен по схеме и в ответ получен нужный результат
- [ ] добавить notExistAction в scenario.repeatebleWhile и в request.expectedResult
- [ ] переименовать repeatableWhile в repeatWhile
- [ ] в properties добавить поле resultLogFile куда будет логироваться только результат
- [ ] добавить несколько логеров с разными уровнями записи.
    По итогу хочу получить такую структуру файлов
    - /dd-MM-yyyy/hh-mm-ss/info.log - логи INFO
    - /dd-MM-yyyy/hh-mm-ss/debug.log - логи DEBUG
    - /dd-MM-yyyy/hh-mm-ss/result.log  - логи результата
- [ ] покрыть код тестами
