import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Assignment 4: original implementations, correctness tests, and reproducible benchmarks. */
public class Main {
    static volatile long sink;
    static final long SEED = 42L;
    static final String ALPHABET = "abcd";
    static long comparisons;

    static int lcs(String a, String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i=1;i<=a.length();i++) for(int j=1;j<=b.length();j++)
            dp[i][j] = a.charAt(i-1)==b.charAt(j-1) ? dp[i-1][j-1]+1 : Math.max(dp[i-1][j],dp[i][j-1]);
        return dp[a.length()][b.length()];
    }
    static int edit(String a,String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];
        for(int i=0;i<=a.length();i++) dp[i][0]=i;
        for(int j=0;j<=b.length();j++) dp[0][j]=j;
        for(int i=1;i<=a.length();i++) for(int j=1;j<=b.length();j++) {
            int cost=a.charAt(i-1)==b.charAt(j-1)?0:1;
            dp[i][j]=Math.min(dp[i-1][j]+1,Math.min(dp[i][j-1]+1,dp[i-1][j-1]+cost));
        }
        return dp[a.length()][b.length()];
    }
    static List<Integer> emptyMatches(int n) {
        List<Integer> ans=new ArrayList<>(); for(int i=0;i<=n;i++) ans.add(i); return ans;
    }
    static List<Integer> naive(String t,String p) {
        List<Integer> ans=new ArrayList<>(); int n=t.length(),m=p.length();
        if(m==0)return emptyMatches(n);
        for(int i=0;i<=n-m;i++) {int j=0;while(j<m){comparisons++;if(t.charAt(i+j)!=p.charAt(j))break;j++;}if(j==m)ans.add(i);}
        return ans;
    }
    static int[] lps(String p) {
        int[] a=new int[p.length()];int len=0,i=1;
        while(i<p.length()){comparisons++;if(p.charAt(i)==p.charAt(len))a[i++]=++len;
        else if(len!=0)len=a[len-1];else a[i++]=0;}
        return a;
    }
    static List<Integer> kmp(String t,String p) {
        List<Integer> ans=new ArrayList<>();int n=t.length(),m=p.length();
        if(m==0)return emptyMatches(n);
        int[] prefix=lps(p);int i=0,j=0;
        while(i<n){comparisons++;if(t.charAt(i)==p.charAt(j)){i++;j++;if(j==m){ans.add(i-m);j=prefix[j-1];}}
        else if(j>0)j=prefix[j-1];else i++;}
        return ans;
    }
    static List<Integer> rabinKarp(String t,String p) {
        List<Integer> ans=new ArrayList<>();int n=t.length(),m=p.length();
        if(m==0)return emptyMatches(n);if(m>n)return ans;
        long mod=1_000_000_007L,base=257L,power=1,ph=0,th=0;
        for(int i=0;i<m;i++){if(i>0)power=power*base%mod;ph=(ph*base+p.charAt(i))%mod;th=(th*base+t.charAt(i))%mod;}
        for(int i=0;i<=n-m;i++){
            if(ph==th){int j=0;while(j<m){comparisons++;if(t.charAt(i+j)!=p.charAt(j))break;j++;}if(j==m)ans.add(i);}
            if(i<n-m){th=(th-t.charAt(i)*power%mod+mod)%mod;th=(th*base+t.charAt(i+m))%mod;}
        }
        return ans;
    }
    /** Kasai: sa is a permutation of [0,n), ordered by suffix lexicographic order. */
    static int[] kasai(String s,int[] sa){
        int n=s.length();if(sa.length!=n)throw new IllegalArgumentException("Invalid suffix array length");
        int[] rank=new int[n],lcp=new int[n];boolean[] seen=new boolean[n];
        for(int i=0;i<n;i++){if(sa[i]<0||sa[i]>=n||seen[sa[i]])throw new IllegalArgumentException("Invalid suffix permutation");seen[sa[i]]=true;rank[sa[i]]=i;}
        int h=0;for(int i=0;i<n;i++){
            int r=rank[i];if(r==0){h=0;continue;}
            int j=sa[r-1];while(i+h<n&&j+h<n&&s.charAt(i+h)==s.charAt(j+h))h++;
            lcp[r]=h;if(h>0)h--;
        }return lcp;
    }
    /** Simple suffix-array construction for benchmarks only; NOT included in Kasai timing. */
    static int[] suffixArray(String s){
        Integer[] order=new Integer[s.length()];for(int i=0;i<order.length;i++)order[i]=i;
        Arrays.sort(order,(x,y)->{int i=x,j=y;while(i<s.length()&&j<s.length()){int c=Character.compare(s.charAt(i++),s.charAt(j++));if(c!=0)return c;}return Integer.compare(s.length()-x,s.length()-y);});
        int[] sa=new int[order.length];for(int i=0;i<sa.length;i++)sa[i]=order[i];return sa;
    }
    static void check(boolean condition,String label){if(!condition)throw new AssertionError(label);System.out.println("PASS "+label);}
    static void matchTest(String text,String pattern,List<Integer> expected,String label){
        comparisons=0;List<Integer> a=naive(text,pattern),b=kmp(text,pattern),c=rabinKarp(text,pattern);
        check(a.equals(expected)&&a.equals(b)&&b.equals(c),label);
    }
    static void tests(){
        check(lcs("abc","abc")==3,"LCS identical");check(lcs("abc","xyz")==0,"LCS different");check(lcs("","abc")==0,"LCS empty");
        check(lcs("ABCBDAB","BDCABA")==4,"LCS classic");
        check(edit("abc","abc")==0,"Edit identical");check(edit("","abc")==3&&edit("abc","")==3,"Edit empty");
        check(edit("cat","cut")==1,"Edit substitution");check(edit("cat","cart")==1,"Edit insertion");
        check(edit("cart","cat")==1,"Edit deletion");check(edit("kitten","sitting")==3,"Edit classic");
        matchTest("abcde","ab",Arrays.asList(0),"Match beginning");matchTest("abcde","bc",Arrays.asList(1),"Match middle");
        matchTest("abcde","de",Arrays.asList(3),"Match end");matchTest("ababa","aba",Arrays.asList(0,2),"Match overlaps");
        matchTest("abc","z",Collections.emptyList(),"Match absent");matchTest("abc","abcd",Collections.emptyList(),"Pattern longer");
        matchTest("abc","",Arrays.asList(0,1,2,3),"Empty pattern all boundaries");
        matchTest("","",Arrays.asList(0),"Both empty");matchTest("aaaaa","aa",Arrays.asList(0,1,2,3),"Repeated matches");
        check(Arrays.equals(kasai("banana",new int[]{5,3,1,0,4,2}),new int[]{0,1,3,0,0,2}),"Kasai banana manual");
        check(Arrays.equals(kasai("",new int[]{}),new int[]{}),"Kasai empty");
        check(Arrays.equals(kasai("aaaa",new int[]{3,2,1,0}),new int[]{0,1,2,3}),"Kasai repeated");
    }
    static String randomString(Random rng,int n){StringBuilder b=new StringBuilder(n);for(int i=0;i<n;i++)b.append(ALPHABET.charAt(rng.nextInt(ALPHABET.length())));return b.toString();}
    interface Work {long run();}
    static double timed(Work w){long t=System.nanoTime();sink^=w.run();return (System.nanoTime()-t)/1_000_000.0;}
    static double average(Work w){for(int i=0;i<2;i++)timed(w);double sum=0;for(int i=0;i<5;i++)sum+=timed(w);return sum/5;}
    static void record(PrintWriter out,String family,String algo,int size,String scenario,double ms,long ops){
        out.printf(Locale.US,"%s,%s,%d,%s,%.6f,%d%n",family,algo,size,scenario,ms,ops);
        System.out.printf(Locale.US,"%-12s %-12s %7d %-8s %10.4f ms %10d ops%n",family,algo,size,scenario,ms,ops);
    }
    static void benchmarks()throws IOException{
        Files.createDirectories(Paths.get("results"));Random rng=new Random(SEED);
        try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(Paths.get("results/benchmarks.csv")))){
            out.println("family,algorithm,size,scenario,avg_ms,operations");
            for(int n:new int[]{100,500,1000}){
                String a=randomString(rng,n),b=randomString(rng,n);
                record(out,"DP","LCS",n,"random",average(()->lcs(a,b)),(long)n*n);
                record(out,"DP","EditDistance",n,"random",average(()->edit(a,b)),(long)n*n);
            }
            for(int n:new int[]{10000,50000,100000}){
                String t=randomString(rng,n);String present=t.substring(n/2,n/2+12),absent="zzzzzzzzzzzz";
                for(String scenario:new String[]{"present","absent"}){
                    String p=scenario.equals("present")?present:absent;
                    for(String algorithm:new String[]{"Naive","KMP","RabinKarp"}){
                        comparisons=0;List<Integer> reference=naive(t,p); // correctness outside timed region
                        Work work=()->{List<Integer> ans;switch(algorithm){case "Naive":ans=naive(t,p);break;case "KMP":ans=kmp(t,p);break;default:ans=rabinKarp(t,p);}if(!ans.equals(reference))throw new AssertionError("Benchmark mismatch");return ans.size();};
                        double ms=average(work);comparisons=0;work.run();long ops=comparisons;
                        record(out,"Matching",algorithm,n,scenario,ms,ops);
                    }
                }
            }
            for(int n:new int[]{1000,5000,10000}){
                String s=randomString(rng,n);int[] sa=suffixArray(s); // excluded from timing
                record(out,"LCP","Kasai",n,"random",average(()->Arrays.hashCode(kasai(s,sa))),n);
            }
        }
    }
    public static void main(String[] args)throws Exception{
        if(args.length==0||args[0].equals("test")){tests();System.out.println("ALL TESTS PASSED");}
        else if(args[0].equals("benchmark")){benchmarks();System.out.println("Saved results/benchmarks.csv");}
        else if(args[0].equals("all")){tests();benchmarks();}
        else System.out.println("Usage: java -cp out Main [test|benchmark|all]");
    }
}

