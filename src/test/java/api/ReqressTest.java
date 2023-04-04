package api;

import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ReqressTest {
    private final static String URL = "https://reqres.in/";

    @Test // Получаем список пользователей со 2й страницы, убеждаемся что имена файлов-аватаров совпадают, убеждаемся что email имеет окончание "reqres.in"
    public void checkAvatarAndIdTest(){
        Spetifications.installSpetification(Spetifications.requestSpec(URL),Spetifications.responseSpecOK200());
        //1й способ сравнивать значения напрямую. из экземпляров классов
        List<UserData> users = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data",UserData.class);
        // По каждому пользователю произошел перебор и мы у каждого пользователя получили аватар и сравнили то что АЙДИшка содержится в аватаре
        users.forEach(x-> Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));

        // проверка почты заканчивается на "reqres.in"
        Assert.assertTrue(users.stream().allMatch(x->x.getEmail().endsWith("@reqres.in")));

        //2 способ сравнивать значаения через получения списков
        //список с аватарками
        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = users.stream().map(x->x.getId().toString()).collect(Collectors.toList());
        for (int i = 0; i < avatars.size(); i++) {
            Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }

    @Test  // Успешная регистрация
    public void successReqTest(){
        Spetifications.installSpetification(Spetifications.requestSpec(URL),Spetifications.responseSpecOK200());
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in","pistol");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);
        Assert.assertNotNull(successReg.getId());
        Assert.assertNotNull(successReg.getToken());

        Assert.assertEquals(id,successReg.getId());
        Assert.assertEquals(token,successReg.getToken());
    }
    @Test // регистрация с ошибкой из за отсутствия пароля и проверка кода ошиббки
    public void unSuccessRegUser(){
        Spetifications.installSpetification(Spetifications.requestSpec(URL),Spetifications.responseSpecERROR400());
        Register user = new Register("sydney@fife","");
        UnSuccessReg unSuccessReg = given()
                .body(user)
                .post("api/register")
                .then().log().all()
                .extract().as(UnSuccessReg.class);
        Assert.assertEquals("Missing password",unSuccessReg.getError());

    }

    @Test // убеждаемся что операция  LIST<RESOURCE> возвращает данные отсортированные по годам
    public void sortedYearsTest(){
        Spetifications.installSpetification(Spetifications.requestSpec(URL),Spetifications.responseSpecOK200());
        List<ColorsData> colors = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data",ColorsData.class);
        List<Integer> years = colors.stream().map(ColorsData::getYear).collect(Collectors.toList());
        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(sortedYears,years);
        System.out.println(years);
        System.out.println(sortedYears);
    }

    @Test // Удаляем второго пользователя и сравниваем статус код
    public void deleteUserTest(){
        Spetifications.installSpetification(Spetifications.requestSpec(URL),Spetifications.responseSpecUnique(204));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }

/*    @Test
    public void timeTest(){
        Spetifications.installSpetification(Spetifications.requestSpec(URL),Spetifications.responseSpecOK200());
        UserTime user = new UserTime("morpheus","zion residen");
        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);
        String regex = "(.{5})$";  // регулярка
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex,"");
        System.out.println(currentTime);
        Assert.assertEquals(currentTime,response.getUpdatedAt().replaceAll(regex,""));
        System.out.println(response.getUpdatedAt().replaceAll(regex,""));
    }*/

}
