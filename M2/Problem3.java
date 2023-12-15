
import java.util.Arrays;

public class Problem3 
{
    public static void main(String[] args) 
    {
        //Don't edit anything here
        Integer[] a1 = new Integer[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        Integer[] a2 = new Integer[]{-1, 1, -2, 2, 3, -3, -4, 5};
        Double[] a3 = new Double[]{-0.01, -0.0001, -.15};
        String[] a4 = new String[]{"-1", "2", "-3", "4", "-5", "5", "-6", "6", "-7", "7"};
        
        bePositive(a1);
        bePositive(a2);
        bePositive(a3);
        bePositive(a4);
    }
    
    static <T> void bePositive(T[] arr) 
    {
        System.out.println("Processing Array:" + Arrays.toString(arr));

        T[] output = Arrays.copyOf(arr, arr.length);

        for (int i = 0; i < arr.length; i++) 
        {
            if (arr[i] instanceof Integer) 
            {
                int intValue = (Integer) arr[i];
                if (intValue < 0) 
                {
                    output[i] = (T) (Integer) (-intValue);
                }
            } 
            /////////////
            else if (arr[i] instanceof Double) 
            {
                double doubleValue = (Double) arr[i];
                if (doubleValue < 0) 
                {
                    output[i] = (T) (Double) (-doubleValue); // Convert to positive and cast back to T
                }
            } 
            /////////////
            else if (arr[i] instanceof String) 
            {
                try 
                {
                    double doubleValue = Double.parseDouble((String) arr[i]);
                    if (doubleValue < 0) 
                    {
                        output[i] = (T) (String) String.valueOf(-doubleValue); 
                    }
                } 
                catch (NumberFormatException e) 
                {
                    
                    System.out.println("Invalid string value: " + arr[i]);
                }
                
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Object i : output) 
        {
            if (sb.length() > 0) 
            {
                sb.append(",");
            }
            sb.append(String.format("%s (%s)", i, i.getClass().getSimpleName().substring(0, 1)));
        }
        System.out.println("Result: " + sb.toString());
    }
}