package zerrium;

public class Top3 {
    String name1, name2, name3;
    long number1, number2, number3;
    public Top3(String name1, long number1, String name2, long number2, String name3, long number3){
        this.name1 = name1.replace("minecraft:", "").replace("_", " ");
        this.number1 = number1;
        this.name2 = name2.replace("minecraft:", "").replace("_", " ");
        this.number2 = number2;
        this.name3 = name3.replace("minecraft:", "").replace("_", " ");
        this.number3 = number3;
    }
}
