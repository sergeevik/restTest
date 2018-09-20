TODO:
- [ ] добавить notExistAction в scenario.repeatebleWhile и в request.expectedResult
- [ ] переименовать repeatableWhile в repeatWhile
- [ ] в properties добавить поле resultLogFile куда будет логироваться только результат
- [ ] добавить несколько логеров с разными уровнями записи.
    По итогу хочу получить такую структуру файлов
    - /dd-MM-yyyy/hh-mm-ss/info.log - логи INFO
    - /dd-MM-yyyy/hh-mm-ss/debug.log - логи DEBUG
    - /dd-MM-yyyy/hh-mm-ss/result.log  - логи результата

FIX:
- [x] добавить Expected Result к запросу, чтобы можно было проверить данные в ответе. 
  Есть разница ответ получен и верен по схеме и в ответ получен нужный результат
- [x] пофиксить jacoco чтобы видел тесты с powerMock
- [x] покрыть код тестами
- [x] добавить [pitest](http://pitest.org/)
