package com.example.scopedvalue;

public class ScopedValueBoundedLifeTime {
    private static final ScopedValue<String> SCOPED_VALUE1 = ScopedValue.newInstance();
    private static final ScopedValue<String> SCOPED_VALUE2 = ScopedValue.newInstance();

    public static void main(String[] args) {
        //ScopedValue.where(SCOPED_VALUE1,"Value1").run(()->method1());

        ScopedValue.Carrier carrier = ScopedValue.where(SCOPED_VALUE1,"Value1").where(SCOPED_VALUE2,"Value2");
        carrier.run(()->method1());
        //ScopedValue.runWhere(SCOPED_VALUE2,"Value2",()->method2());

    }

    public static void method1() {
        if(SCOPED_VALUE1.isBound())
        {
            System.out.println("method 1 , value of SCOPED_VALUE1 :"+SCOPED_VALUE1.get());
        }
        else
        {
            System.out.println("method 1 , SCOPED_VALUE1 is not  bound !");
        }
        if(SCOPED_VALUE2.isBound())
        {
            System.out.println("method 1 , value of SCOPED_VALUE2 :"+SCOPED_VALUE2.get());
        }
        else
        {
            System.out.println("method 1 , SCOPED_VALUE2 is not  bound !");
        }

        //method2();


    }

    public static void method2() {
        if(SCOPED_VALUE1.isBound())
        {
            System.out.println("method 2 , value of SCOPED_VALUE1 :"+SCOPED_VALUE1.get());
        }
        else
        {
            System.out.println("method 2 , SCOPED_VALUE1 is not  bound !");
        }
        if(SCOPED_VALUE2.isBound())
        {
            System.out.println("method 2 , value of SCOPED_VALUE2 :"+SCOPED_VALUE2.get());
        }
        else
        {
            System.out.println("method 2 , SCOPED_VALUE2 is not  bound !");
        }

    }
}
