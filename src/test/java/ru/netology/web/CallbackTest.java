package ru.netology.web;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

class CallbackTest {
    public String dateGenerator(int days, String pattern) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(pattern));
    }

    protected void cityFindAndSet(String city) {
        $$("[data-test-id=city] input").find(Condition.visible).setValue(city);
    }

    protected void dateFindAndSet(int days) {
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(days, "dd.MM.yyyy"));
    }

    protected void nameFindAndSet(String name) {
        $("[data-test-id=name] input").setValue(name);
    }

    protected void phoneFindAndSet(String phone) {
        $$("[data-test-id=phone] input").find(Condition.visible).setValue(phone);
    }

    protected void findAndClickSubmitButton() {
        $$("button").find(exactText("Забронировать")).click();
    }

    protected void findAndClickAgreement() {
        $$("[data-test-id=agreement]").find(Condition.visible).click();
    }

    @BeforeEach
    public void setup() {
        open("http://localhost:9999");
    }


    @ParameterizedTest
    @CsvFileSource(files = "src/test/resources/shouldSendValidForm.csv", numLinesToSkip = 1)
    void shouldSendValidForm(String city, int days, String name, String phone) {
        cityFindAndSet(city);
        dateFindAndSet(days);
        nameFindAndSet(name);
        phoneFindAndSet(phone);
        findAndClickAgreement();
        findAndClickSubmitButton();
        $(Selectors.byText("Встреча успешно забронирована на")).should(Condition.visible, Duration.ofSeconds(15));
    }

    @ParameterizedTest
    @CsvFileSource(files = "src/test/resources/shouldNotSendValidFormWithInvalidName.csv", numLinesToSkip = 1)
    void shouldNotSendFormWithInvalidName(String city, int days, String name, String phone) {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(days, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue(name);
        $$("[data-test-id=phone] input").find(Condition.visible).setValue(phone);
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=name].input_invalid").should(Condition.visible);
        $$("[data-test-id=name].input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @ParameterizedTest
    @CsvFileSource(files = "src/test/resources/shouldNotSendFormWithInvalidPhone.csv", numLinesToSkip = 1)
    void shouldNotSendFormWithInvalidPhone(String phone) {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue(phone);
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=phone].input_invalid").should(Condition.visible);
        $$("[data-test-id=phone].input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678"));
    }

    @ParameterizedTest
    @CsvFileSource(files = "src/test/resources/shouldNotSendFormWithInvalidCity.csv", numLinesToSkip = 1)
    void shouldNotSendFormWithInvalidCity(String city) {
        $$("[data-test-id=city] input").find(Condition.visible).setValue(city);
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=city].input_invalid").should(Condition.visible);
        $$("[data-test-id=city].input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldNotSendFormWithInvalidDate() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(2, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=date] span.input_invalid").should(Condition.visible);
        $$("[data-test-id=date] span.input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Заказ на выбранную дату невозможен"));
    }

    @Test
    void shouldNotSendFormWithInvalidDateInFuture() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue("31.02.2145");
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=date] span.input_invalid").should(Condition.visible);
        $$("[data-test-id=date] span.input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldNotSendFormWithEmptyName() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("  ");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=name].input_invalid").should(Condition.visible);
        $$("[data-test-id=name].input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Поле обязательно для заполнения"));
    }

    @Test
    void shouldNotSendFormWithEmptyPhone() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("  ");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=phone].input_invalid").should(Condition.visible);
        $$("[data-test-id=phone].input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Поле обязательно для заполнения"));
    }

    @Test
    void shouldNotSendFormWithEmptyCheckbox() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=agreement].input_invalid").should(Condition.visible);
    }

    @Test
    void shouldNotSendFormWithEmptyCity() {
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=city].input_invalid").should(Condition.visible);
        $$("[data-test-id=city].input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Поле обязательно для заполнения"));
    }

    @Test
    void shouldNotSendFormWithEmptyDate() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(" ");
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=date] span.input_invalid").should(Condition.visible);
        $$("[data-test-id=date] span.input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldNotSendFormWithUncorrectDate() {
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("Вася");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $("[data-test-id=date] span.input_invalid").should(Condition.visible);
        $$("[data-test-id=date] span.input_invalid span.input__sub").find(Condition.visible)
                .shouldHave(text("Неверно введена дата"));
    }

    @ParameterizedTest
    @CsvFileSource(files = "src/test/resources/shouldSearchCityWithTwoSymbols.csv")
    void shouldSendFormSearchCityWithTwoSymbols(String querySymbols, String cityToFind) {
        $$("[data-test-id=city] input").find(Condition.visible).setValue(querySymbols);
        $$("div.popup div.menu-item").find(Condition.text(cityToFind)).click();
        $$("[data-test-id=date] input").find(Condition.visible)
                .press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE).setValue(dateGenerator(3, "dd.MM.yyyy"));
        $("[data-test-id=name] input").setValue("ВАСЯ ПЕТРОВ");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $(Selectors.byText("Встреча успешно забронирована на")).should(Condition.visible, Duration.ofSeconds(15));

//        char[] cityInTable = $$("div.popup div.menu-item").first().getText().toLowerCase().toCharArray();
//        char[] cityInSearchQuery = querySymbols.toLowerCase().toCharArray();
//        char[] resultArr = new char[cityInSearchQuery.length];
//        for (int i = 0; i < resultArr.length; i++) {
//            resultArr[i] = cityInTable[i];
//        }


    }

    @Test
    void shouldAddDateInCalendar() {
        LocalDate dayNextWeek = LocalDate.now().plusDays(7);
        String newDay = Integer.toString(dayNextWeek.getDayOfMonth());
        //int month = LocalDate.now().plusDays(7).getMonthValue();
        open("http://localhost:9999");
        $$("[data-test-id=city] input").find(Condition.visible).setValue("Симферополь");
        $$("[data-test-id=date] button").find(Condition.visible).click();
        if (LocalDate.now().getMonthValue() != dayNextWeek.getMonthValue()) {
            $("div.popup div.calendar__arrow_direction_right[data-step='1']").click();
            $$("div.popup td.calendar__day").find(Condition.text(newDay)).click();
        } else {
            $$("div.popup td.calendar__day").find(Condition.text(newDay)).click();
        }
        $("[data-test-id=name] input").setValue("ВАСЯ ПЕТРОВ");
        $$("[data-test-id=phone] input").find(Condition.visible).setValue("+79114359999");
        $$("[data-test-id=agreement]").find(Condition.visible).click();
        $$("button").find(exactText("Забронировать")).click();
        $(Selectors.byText("Встреча успешно забронирована на")).should(Condition.visible, Duration.ofSeconds(15));
    }
}

