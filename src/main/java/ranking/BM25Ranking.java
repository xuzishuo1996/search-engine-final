package ranking;

public class BM25Ranking {
    public static final int N = 131896; // total number of docs in the collection
    public static final double AVE_DOC_LEN = 507.81437647843757;   // stemmed average document length. calculate when indexing
    // parameters in BM25 formula (could be tuned)
    static double k1 = 1.2;
    static double b = 0.75;
    static double k2 = 7;

    // core formula
    public static double calcSingleTermScoreForADoc(int freqInDoc, int freqInQuery, int docLength, int containingDocNum) {
        double K = k1 * (1 - b + b * (double) docLength / AVE_DOC_LEN);
        double tfInDocPart = (k1 + 1) * (double) freqInDoc / (K + freqInDoc);
        double tfInQueryPart = (k2 + 1) * (double) freqInQuery / (k2 + freqInQuery);
        double idfPart =  Math.log((N - containingDocNum + 0.5) / (containingDocNum + 0.5));   // ln
        return tfInDocPart * tfInQueryPart * idfPart;
    }
}
