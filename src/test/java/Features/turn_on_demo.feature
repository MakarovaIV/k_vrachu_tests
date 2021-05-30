Feature: Turn on demo mode

Scenario: Demo mode enabled successfully
    Given Portal k-vrachu.ru
    When Click btn turn on demo
    And If no demo choose another region
    And Open appointment page to doctor
    And Choose doctor
    And Make record
    And Confirm record
    And Message "Запись в базу данных невозможна" appears (you can't make an appointment)
    Then Log out
