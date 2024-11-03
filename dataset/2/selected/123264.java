package util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import entitybeans.Comments;
import entitybeans.Favorites;
import entitybeans.Friends;
import entitybeans.Ingredients;
import entitybeans.Lists;
import entitybeans.RecentlyViewed;
import entitybeans.RecipeIngredients;
import entitybeans.RecipeInstructions;
import entitybeans.Recipes;
import entitybeans.Users;

public class InitDB {

    public static Session session = HibernateUtil.getSession();

    public static void createRecipe(Iterable<String> enumeration) {
        for (String s : enumeration) {
            System.out.println(s);
        }
    }

    public static void createRecipeIngredients(Recipes recipe, Ingredients ingredient, double quantity, String measure) {
        RecipeIngredients recing = new RecipeIngredients(recipe, ingredient, quantity, measure);
        session.save(recing);
    }

    public static void createRecipeInstructions(Recipes recipe, String instruction) {
        RecipeInstructions recIns = new RecipeInstructions(recipe, instruction);
        session.save(recIns);
    }

    public static void createComment(Recipes rec, int grade, Users user, String comment) {
        Comments com = new Comments(rec, user, comment, grade, new Date());
        session.save(com);
    }

    public static void CHANGE_NAME(String[] args) {
        List<String> arr = Arrays.asList("a", "b");
        createRecipe(arr);
    }

    public static Recipes addRecipe(Lists complexity, String about, String title, Users user, int preparationTime, int cookingTime, int servings, Lists dishType, String picUrl, Iterable<String> instructions) throws Exception {
        URL url = new URL(picUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        Recipes rec = new Recipes(user, title, about, preparationTime, cookingTime, servings, complexity, dishType, Hibernate.createBlob(conn.getInputStream(), conn.getContentLength()), new Date(), 0);
        session.save(rec);
        for (String s : instructions) {
            createRecipeInstructions(rec, s);
        }
        return rec;
    }

    public static void main(String[] args) {
        try {
            Transaction transaction = session.beginTransaction();
            URL url1 = new URL("http://www.cs.huji.ac.il/~keren_ha/J2EE/keren.jpg");
            HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
            conn1.connect();
            Users keren = new Users("kerenhaas@gmail.com", "123456", "keren", null, "sokolov 14 Raanana", Hibernate.createBlob(conn1.getInputStream(), conn1.getContentLength()), "about keren", "admin", false);
            session.save(keren);
            session.flush();
            session.refresh(keren);
            URL url2 = new URL("http://www.cs.huji.ac.il/~keren_ha/J2EE/alex.jpg");
            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
            Users alex = new Users("alex.uretsky@mail.huji.ac.il", "123456", "alex", null, null, Hibernate.createBlob(conn2.getInputStream(), conn2.getContentLength()), null, "admin", false);
            session.save(alex);
            session.flush();
            session.refresh(alex);
            URL url3 = new URL("http://www.cs.huji.ac.il/~keren_ha/J2EE/julia.jpg");
            HttpURLConnection conn3 = (HttpURLConnection) url3.openConnection();
            conn3.connect();
            Users julia = new Users("juliasht@gmail.com", "123456", "julia", null, null, Hibernate.createBlob(conn3.getInputStream(), conn3.getContentLength()), null, "admin", false);
            session.save(julia);
            session.flush();
            session.refresh(julia);
            URL url4 = new URL("http://www.cs.huji.ac.il/~keren_ha/J2EE/meir.jpg");
            HttpURLConnection conn4 = (HttpURLConnection) url4.openConnection();
            conn4.connect();
            Users meir = new Users("meir.spielrein@mail.huji.ac.il", "123456", "meir", null, null, Hibernate.createBlob(conn4.getInputStream(), conn4.getContentLength()), null, "admin", false);
            session.save(meir);
            session.flush();
            session.refresh(meir);
            URL url5 = new URL("http://www.cs.huji.ac.il/~keren_ha/J2EE/miki.jpg");
            HttpURLConnection conn5 = (HttpURLConnection) url5.openConnection();
            conn5.connect();
            Users miki = new Users("miki.shifman@mail.huji.ac.il", "123456", "miki", null, null, Hibernate.createBlob(conn5.getInputStream(), conn5.getContentLength()), null, "admin", false);
            session.save(miki);
            session.flush();
            session.refresh(miki);
            URL url6 = new URL("http://www.cs.huji.ac.il/~keren_ha/J2EE/tamar.jpg");
            HttpURLConnection conn6 = (HttpURLConnection) url6.openConnection();
            conn6.connect();
            Users tami = new Users("taamar@gmail.com", "123456", "tami", null, null, Hibernate.createBlob(conn6.getInputStream(), conn6.getContentLength()), null, "admin", false);
            session.save(tami);
            session.flush();
            session.refresh(tami);
            Lists basicComplexity = new Lists("Complexity", "Basic");
            session.save(basicComplexity);
            Lists mediumComplexity = new Lists("Complexity", "Medium");
            session.save(mediumComplexity);
            Lists highComplexity = new Lists("Complexity", "High");
            session.save(highComplexity);
            Lists chefComplexity = new Lists("Complexity", "Chef");
            session.save(chefComplexity);
            Lists appetizers = new Lists("DishType", "Appetizers");
            session.save(appetizers);
            Lists firstCourse = new Lists("DishType", "First Course");
            session.save(firstCourse);
            Lists mainCourse = new Lists("DishType", "Main Course");
            session.save(mainCourse);
            Lists dessert = new Lists("DishType", "Dessert");
            session.save(dessert);
            Lists cocktails = new Lists("DishType", "Cocktails");
            session.save(cocktails);
            System.out.println("2 : " + session.isOpen());
            Lists italian = new Lists("Cuisine", "Italian");
            session.save(italian);
            Lists chinese = new Lists("Cuisine", "Chinese");
            session.save(chinese);
            Lists indian = new Lists("Cuisine", "Indian");
            session.save(indian);
            Lists french = new Lists("Cuisine", "French");
            session.save(french);
            Lists thai = new Lists("Cuisine", "Thai");
            session.save(thai);
            Lists arabic = new Lists("Cuisine", "Arabic");
            session.save(arabic);
            Lists israeli = new Lists("Cuisine", "Israeli");
            session.save(israeli);
            Lists other = new Lists("Cuisine", "Other");
            session.save(other);
            Ingredients flour = new Ingredients("flour");
            session.save(flour);
            Ingredients sugar = new Ingredients("white sugar");
            session.save(sugar);
            Ingredients bakingPower = new Ingredients("baking powder");
            session.save(bakingPower);
            Ingredients groundNutmeg = new Ingredients("ground nutmeg");
            session.save(groundNutmeg);
            Ingredients salt = new Ingredients("salt");
            session.save(salt);
            Ingredients pepper = new Ingredients("pepper");
            session.save(pepper);
            Ingredients egg = new Ingredients("egg");
            session.save(egg);
            Ingredients milk = new Ingredients("milk");
            session.save(milk);
            Ingredients butter = new Ingredients("butter");
            session.save(butter);
            Ingredients groundCinnamon = new Ingredients("ground cinnamon");
            session.save(groundCinnamon);
            Ingredients strawberries = new Ingredients("strawberries");
            session.save(strawberries);
            Ingredients bisquick = new Ingredients("bisquick");
            session.save(bisquick);
            Ingredients whippedCream = new Ingredients("Whipped Cream");
            session.save(whippedCream);
            Ingredients potato = new Ingredients("potato");
            session.save(potato);
            Ingredients carrot = new Ingredients("carrot");
            session.save(carrot);
            Ingredients onion = new Ingredients("onion");
            session.save(onion);
            Ingredients ketchup = new Ingredients("ketchup");
            session.save(ketchup);
            Ingredients mustard = new Ingredients("mustard");
            session.save(mustard);
            Ingredients cookingCream = new Ingredients("Cooking Cream");
            session.save(cookingCream);
            Ingredients bread = new Ingredients("bread");
            session.save(bread);
            Ingredients caviar = new Ingredients("caviar");
            session.save(caviar);
            Ingredients foigra = new Ingredients("foigra");
            session.save(foigra);
            Ingredients vodka = new Ingredients("vodka");
            session.save(vodka);
            Ingredients orangeJuice = new Ingredients("orangeJuice");
            session.save(orangeJuice);
            Ingredients ribs = new Ingredients("ribs");
            session.save(ribs);
            Ingredients tomato = new Ingredients("tomato");
            session.save(tomato);
            Ingredients cucumber = new Ingredients("cucumber");
            session.save(cucumber);
            Ingredients oliveoil = new Ingredients("olive oil");
            session.save(oliveoil);
            Ingredients chickenBreast = new Ingredients("chicken Breast");
            session.save(chickenBreast);
            Ingredients apple = new Ingredients("apple");
            session.save(apple);
            Ingredients vanilla = new Ingredients("vanilla");
            session.save(vanilla);
            String description;
            description = "These muffins are delicious! The cinnamon sugar topping flavors them perfectly. This is my 10 year old brother's favorite recipe";
            Recipes rec1 = addRecipe(basicComplexity, description, "French Breakfast Muffins", keren, 10, 25, 12, dessert, "http://www.cs.huji.ac.il/~keren_ha/J2EE/muffins.jpg", Arrays.asList("Preheat oven to 350 degrees F (175 degrees C). Grease muffin cups or line with paper muffin liners.", "In a medium mixing bowl, stir together flour, 1/2 cup sugar, baking powder, nutmeg and salt. Make a well in the center of the mixture. Stir together egg, milk and 1/3 cup melted butter. Add egg mixture to flour mixture; stir until just moistened (batter may be lumpy). Spoon batter into prepared muffin cups.", "Bake in preheated oven for 20 to 25 minutes. Meanwhile, combine 1/4 cup sugar, cinnamon When muffins are finished baking, dip tops of muffins in the melted butter, and then in the cinnamon sugar mixture. Serve warm."));
            createRecipeIngredients(rec1, flour, 1.5, "cups");
            createRecipeIngredients(rec1, sugar, 0.5, "cups");
            createRecipeIngredients(rec1, bakingPower, 1.5, "teaspoons");
            createRecipeIngredients(rec1, groundNutmeg, 0.25, "teaspoons");
            createRecipeIngredients(rec1, salt, 0.125, "teaspoons");
            createRecipeIngredients(rec1, egg, 1, "lightly beaten");
            createRecipeIngredients(rec1, milk, 0.5, "cups");
            createRecipeIngredients(rec1, butter, 0.33, "cups");
            createRecipeIngredients(rec1, groundCinnamon, 0.25, "cups");
            createRecipeIngredients(rec1, groundCinnamon, 0.5, "teaspoon");
            createRecipeIngredients(rec1, sugar, 0.33, "cups");
            session.flush();
            session.refresh(rec1);
            description = "This dish leaves even the biggest pasta lover satisfied. Fresh vegetables make this dish wonderful and it's easy to add meat to if you wish.";
            Recipes rec2 = addRecipe(mediumComplexity, description, "Veggie Pasta Minus the Pasta", alex, 30, 40, 6, mainCourse, "http://2.bp.blogspot.com/_wAVccjOeYzc/R4KYRa5MkLI/AAAAAAAAGz8/WeosqyuyjoQ/s400/vegetarian-tofu-curry-recipe+(13).JPG", Arrays.asList("Preheat an oven to 350 degrees F (175 degrees C). Arrange the tomatoes on a baking sheet with the cut sides facing up. ", "Roast the tomatoes in the preheated oven until cooked through and slightly browned on the underside, about 15 minutes. ", "Place squash halves face down in glass baking dish with the water; cover with plastic wrap. Microwave on High for 8 minutes. Leave covered and set aside. Once the squash is cool enough to handle, scrape in strands into a large bowl with a fork; season with salt and pepper and toss with 1 tablespoon olive oil. ", "Heat the remaining 2 tablespoons olive oil in a large skillet over medium-low heat; cook and stir the garlic, basil, and Italian seasoning in the oil until the garlic is softened, about 10 minutes. Add the onion, green bell pepper, eggplant, and carrot to the garlic; increase heat to medium. Continue cooking and stirring until the vegetables are nearly tender, 10 to 15 minutes. Mix the tomatoes and white wine into the vegetable mixture; cook another 2 to 3 minutes. Transfer the vegetables to the bowl with the spaghetti squash; gently toss together."));
            createRecipeIngredients(rec2, flour, 1.5, "cups");
            createRecipeIngredients(rec2, sugar, 0.5, "cups");
            createRecipeIngredients(rec2, bakingPower, 1.5, "teaspoons");
            createRecipeIngredients(rec2, groundNutmeg, 0.25, "teaspoons");
            createRecipeIngredients(rec2, salt, 0.125, "teaspoons");
            createRecipeIngredients(rec2, egg, 1, "lightly beaten");
            createRecipeIngredients(rec2, milk, 0.5, "cups");
            createRecipeIngredients(rec2, butter, 0.33, "cups");
            createRecipeIngredients(rec2, groundCinnamon, 0.25, "cups");
            createRecipeIngredients(rec2, groundCinnamon, 0.5, "teaspoon");
            createRecipeIngredients(rec2, sugar, 0.33, "cups");
            createComment(rec2, 1, julia, "This dish was extremely disappointing. I was very optimistic looking at the ingredient list, but after putting it all together, there was a profound lack of flavor. We eat a good deal of vegetable dishes, but this is not one we will be repeating.");
            createComment(rec2, 4, alex, "Very good, I also think to use less butter.");
            session.flush();
            session.refresh(rec2);
            description = "A finger licking good strawberry cake!";
            Recipes rec3 = addRecipe(basicComplexity, description, "Strawberry short cake", alex, 60, 70, 12, dessert, "http://static.open.salon.com/files/coconut_strawberry_cake1226877577.jpg", Arrays.asList("Sprinkle strawberries with 2/3 cups sugar. Let stand 1 hour ", "Heat over to 425 degrees.", "Mix all ingredients and place in the over", "Slice it and eat up!"));
            createRecipeIngredients(rec3, strawberries, 1.5, "cups");
            createRecipeIngredients(rec3, sugar, 0.66, "cups");
            createRecipeIngredients(rec3, bisquick, 2, "boxese");
            createRecipeIngredients(rec3, sugar, 3, "tablespoons");
            createRecipeIngredients(rec3, milk, 0.5, "cups");
            createRecipeIngredients(rec3, whippedCream, 0.75, "cups");
            createComment(rec3, 5, julia, "Best cake I ever had!!! Kudos!!");
            session.flush();
            session.refresh(rec3);
            description = "My secret Barbecue Beef Short Ribs recipe revealed!";
            Recipes rec4 = addRecipe(highComplexity, description, "Short Ribs", meir, 70, 500, 6, mainCourse, "http://farm2.static.flickr.com/1310/1237575824_9068241a81.jpg", Arrays.asList("Put the potatoes and carrots in a large slow cooker", "Top with the onion wedges then the beef", "Combine the ketchup, , mustrard and salt", "Put ofver the beef", "Cook on LOW for 8 to 10 hours"));
            createRecipeIngredients(rec4, potato, 10, "pieces");
            createRecipeIngredients(rec4, carrot, 1, "cups");
            createRecipeIngredients(rec4, onion, 2, "units");
            createRecipeIngredients(rec4, ribs, 3.5, "pounds");
            createRecipeIngredients(rec4, ketchup, 1, "cups");
            createRecipeIngredients(rec4, mustard, 0.5, "teaspoon");
            createComment(rec4, 4, keren, "My whole family loved it!");
            createComment(rec4, 5, alex, "This was outstanding, will definitely use this recipe often. I used chicken legs as that was what I had and it worked great. thanks!!!!");
            session.flush();
            session.refresh(rec4);
            description = "a simple, yet yasty, Salad";
            Recipes rec5 = addRecipe(basicComplexity, description, "Garden Salad", alex, 10, 15, 6, firstCourse, "http://ww-recipes.net/wp-content/uploads/2008/09/weight-watchers-arabic-salad-recipe.jpg", Arrays.asList("Slice the tomatoes to cubes", "Slice the cucumbers to julian strips", "add a pinch salt and pepper", "top with olive oil"));
            createRecipeIngredients(rec5, tomato, 3, "pieces");
            createRecipeIngredients(rec5, cucumber, 3, "pieces");
            createRecipeIngredients(rec5, oliveoil, 2, "tablespoons");
            createRecipeIngredients(rec5, salt, 1, "pinch");
            createRecipeIngredients(rec5, pepper, 1, "pinch");
            createComment(rec5, 5, julia, "Delicious and so easy to make!");
            session.flush();
            session.refresh(rec5);
            description = "Creme Brule - the full recipe! no shortcuts ;) ";
            Recipes rec7 = addRecipe(highComplexity, description, "Creme brule", miki, 30, 200, 6, dessert, "http://www.cookingforengineers.com/hello/259/958/640/IMG_3335_sharp.jpg", Arrays.asList("bring the cooking cream to boiling temperature", "insert vanilla stick", "whisk eggs with sugar", "add cream to eggs CAREFULLY", "put in over for 30 minutes, then to the fridge for 4 hours"));
            createRecipeIngredients(rec7, cookingCream, 2, "cartons");
            createRecipeIngredients(rec7, egg, 3, "yolks");
            createRecipeIngredients(rec7, sugar, 2, "tablespoons");
            createRecipeIngredients(rec7, vanilla, 1, "stick");
            createComment(rec7, 4, julia, "Delicious and so easy to make!");
            session.flush();
            session.refresh(rec7);
            description = "some toasts with foigra to get the meal started";
            Recipes rec8 = addRecipe(basicComplexity, description, "foigra on toast", alex, 15, 15, 6, appetizers, "http://www.italiq-expos.com/news/images/Gastronomie/Foie-gras/assiette-foie-gras.jpg", Arrays.asList("cut bread into oval slices", "place in toaster until a golden brown color in formed", "speard some foigra pate on the toasts", "optional - add some baluga caviar on top"));
            createRecipeIngredients(rec8, bread, 6, "slices");
            createRecipeIngredients(rec8, foigra, 1, "can");
            createRecipeIngredients(rec8, caviar, 1, "minijar");
            createComment(rec8, 5, julia, "Delicious and so easy to make!");
            session.flush();
            session.refresh(rec8);
            description = "delicious diatetic chicken steak";
            Recipes rec9 = addRecipe(mediumComplexity, description, "chicken steak", meir, 15, 20, 2, mainCourse, "http://4.bp.blogspot.com/_jhlSdMizhlU/RdOVtm-0QAI/AAAAAAAAABg/81W-JvXOACI/s400/Chicken_Steak.jpg", Arrays.asList("Heat a frying pan with some (preferably olive) oil", "when the oil is hot, place the chicken and onions in the middle", "fry on both sides on medium flame until it starts to turn golden", "season with salt and pepper"));
            createRecipeIngredients(rec9, chickenBreast, 2, "pieces");
            createRecipeIngredients(rec9, onion, 1, "piece");
            createRecipeIngredients(rec9, salt, 1, "pinch");
            createRecipeIngredients(rec9, pepper, 1, "pinch");
            createComment(rec9, 5, julia, "Delicious and so easy to make!");
            session.flush();
            session.refresh(rec9);
            Favorites fav1 = new Favorites(keren, rec1, null);
            session.save(fav1);
            Favorites fav2 = new Favorites(keren, rec2, null);
            session.save(fav2);
            Favorites fav3 = new Favorites(keren, rec3, null);
            session.save(fav3);
            Favorites fav4 = new Favorites(keren, rec4, null);
            session.save(fav4);
            Favorites fav5 = new Favorites(alex, rec4, null);
            session.save(fav5);
            Favorites fav6 = new Favorites(alex, rec2, null);
            session.save(fav6);
            RecentlyViewed recViewed1 = new RecentlyViewed(keren, rec1, new Date());
            session.save(recViewed1);
            RecentlyViewed recViewed2 = new RecentlyViewed(keren, rec2, new Date());
            session.save(recViewed2);
            Friends friend1 = new Friends(keren, alex, true);
            session.save(friend1);
            Friends friend2 = new Friends(alex, keren, true);
            session.save(friend2);
            Friends friend3 = new Friends(keren, julia, false);
            session.save(friend3);
            Friends friend4 = new Friends(keren, meir, true);
            session.save(friend4);
            Friends friend5 = new Friends(meir, keren, true);
            session.save(friend5);
            Friends friend6 = new Friends(tami, keren, false);
            session.save(friend6);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
    }
}
