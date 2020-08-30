package com.advertisement.service.impl;

import com.advertisement.dtos.Ads;
import com.advertisement.service.ScrapService;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ScrapServiceImpl implements ScrapService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapServiceImpl.class);

    private static List<Ads> adsList = new ArrayList<>();

    static {
        Ads ads1 = new Ads(Arrays.asList("book"), "https://powells-covers-2.s3.amazonaws.com/9780312429218.jpg", UUID.randomUUID().toString(), "https://www.powells.com/25-books-to-read-before-you-die");
        Ads ads2 = new Ads(Arrays.asList("keyboard", "software"), "https://i.pcmag.com/imagery/reviews/07vjkhMyPEwY0l5cKyGvdqa-13.fit_lim.size_75x75.v_1579215814.jpg", UUID.randomUUID().toString(), "https://in.pcmag.com/keyboards/44346/the-best-keyboards-for-2020");
        Ads ads3 = new Ads(Arrays.asList("shoe"), "https://i.insider.com/5e382a935bc79c247c1cbc08?width=1200", UUID.randomUUID().toString(), "https://www.businessinsider.in/retail/news/see-the-list-of-the-top-10-best-selling-shoes-of-2019-which-is-completely-dominated-by-nike/articleshow/73912052.cms");
        Ads ads4 = new Ads(Arrays.asList("dog"), "https://www.thehonestkitchen.com/blog/wp-content/uploads/2018/05/Golden.jpg", UUID.randomUUID().toString(), "https://www.thehonestkitchen.com/blog/top-ten-dog-breeds-why-theyre-on-the-list/");
        Ads ads5 = new Ads(Arrays.asList("dog", "book"), "https://cdn2-www.dogtime.com/assets/uploads/2019/01/dog-training-books-1.jpg", UUID.randomUUID().toString(), "https://dogtime.com/reference/dog-training/50703-10-top-rated-classic-dog-training-books");
        Ads ads6 = new Ads(Arrays.asList("mouse"), "https://cdn.mos.cms.futurecdn.net/te352F2DgAYvfxBRtF97KS-650-80.jpg.webp", UUID.randomUUID().toString(), "https://www.creativebloq.com/design-tools/mice-4132486");

        adsList.add(ads1);
        adsList.add(ads2);
        adsList.add(ads3);
        adsList.add(ads4);
        adsList.add(ads5);
        adsList.add(ads6);
    }

    private WebClient client = new WebClient();

    {
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
    }

    //    @PostConstruct
    public void init() throws IOException {
//        scrap("https://www.thehonestkitchen.com/blog/top-ten-dog-breeds-why-theyre-on-the-list/");
        scrap("https://dogtime.com/reference/dog-training/50703-10-top-rated-classic-dog-training-books");
    }

    @Override
    public Ads scrap(String htmlURL) throws IOException {
        Optional<String> htmlText = htmlToText(htmlURL);
        int minDistance = Integer.MAX_VALUE;
        List<Ads> selectedAdsList = new ArrayList<>();
        if (htmlText.isPresent()) {
            String[] tokens = generateTokens(htmlText.get());
            Map<String, Float> termFreqs = termFreq(getPOSTagging(tokens));
            LOGGER.info("termFreqs = {} ", termFreqs);
            LevenshteinDetailedDistance levenshteinDetailedDistance = new LevenshteinDetailedDistance();
            for (Ads ads : adsList) {
                for (String tag : termFreqs.keySet()) {
                    LevenshteinResults levenshteinResults = levenshteinDetailedDistance.apply(ads.getTags().get(0), tag);
                    if (minDistance > levenshteinResults.getDistance()) {
                        selectedAdsList.clear();
                        minDistance = levenshteinResults.getDistance();
                        selectedAdsList.add(ads);
                    } else if (minDistance == levenshteinResults.getDistance()) {
                        selectedAdsList.add(ads);
                    }
                }
            }
        }
        LOGGER.info("selectedAds = {} ", selectedAdsList);
        return CollectionUtils.isEmpty(selectedAdsList) ? null : selectedAdsList.get(Math.abs(new Random().nextInt() % selectedAdsList.size()));
    }

    private Optional<String> htmlToText(String htmlURL) {
        try {
            LOGGER.info("htmlURL = {} ", htmlURL);
            HtmlPage page = client.getPage(htmlURL);
            return Optional.of(removeStopWords(page.asText()));
        } catch (Exception e) {
            LOGGER.error("Exception occurred while scraping html = {} e = {}", htmlURL, e);
        }
        return Optional.empty();
    }

    private String removeStopWords(String inputText) {
        String[] stopWords = new String[]{
            "a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thick", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the"};

        String stopWordsPattern = String.join("|", stopWords);
        Pattern pattern = Pattern.compile("\\b(?:" + stopWordsPattern + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputText);
        return matcher.replaceAll("");
    }

    private String[] generateTokens(String text) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(text);

        return tokens;
    }

    private String[] lemmatization(String[] tokens, String[] tags) throws IOException {
        InputStream dictLemmatizer = ScrapServiceImpl.class.getClassLoader()
            .getResourceAsStream("models/en-lemmatizer.dict");
        DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(
            dictLemmatizer);
        String[] lemmas = lemmatizer.lemmatize(tokens, tags);
        return lemmas;
    }

    private List<String> getNames(String[] tokens) throws IOException {
        List<String> nameList = new ArrayList<>();
        InputStream inputStreamNameFinder = ScrapServiceImpl.class.getClassLoader()
            .getResourceAsStream("models/en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(
            inputStreamNameFinder);
        NameFinderME nameFinderME = new NameFinderME(model);
        List<Span> spans = Arrays.asList(nameFinderME.find(tokens));
        for (Span s : spans) {
            nameList.add(tokens[s.getStart()]);
        }
        return nameList;
    }

    private List<String> getPOSTagging(String[] tokens) throws IOException {
        List<String> tokenList = new ArrayList<>();
        InputStream inputStreamNameFinder = ScrapServiceImpl.class.getClassLoader()
            .getResourceAsStream("models/en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStreamNameFinder);
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String tags[] = posTagger.tag(tokens);
        String[] lemmaTokens = lemmatization(tokens, tags);
        int index = 0;
        for (String tag : tags) {
            if (tag.indexOf("NN") != -1) {
                if (!lemmaTokens[index].contains("O")) {
                    tokenList.add(lemmaTokens[index]);
                }
            }
            index++;
        }
        return tokenList;
    }

    private List<String> getLocations(String[] tokens) throws IOException {
        List<String> locationList = new ArrayList<>();
        InputStream inputStreamNameFinder = ScrapServiceImpl.class.getClassLoader()
            .getResourceAsStream("models/en-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(
            inputStreamNameFinder);
        NameFinderME nameFinderME = new NameFinderME(model);
        List<Span> spans = Arrays.asList(nameFinderME.find(tokens));
        for (Span s : spans) {
            locationList.add(tokens[s.getStart()]);
        }
        return locationList;
    }

    private Map<String, Float> termFreq(List<String> tokens) {
        Map<String, Long> wordFreqCountMap = tokens.stream().filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Long sum = wordFreqCountMap.values().stream().reduce(0L, (a, b) -> a + b);

        Map<String, Float> termFreq = new HashMap<>();

        wordFreqCountMap.entrySet().stream().forEach(entry -> termFreq.put(entry.getKey(), entry.getValue() * 1.0f / sum));

        return termFreq.entrySet()
            .stream()
            .sorted((Map.Entry.<String, Float>comparingByValue().reversed())).limit(10)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static class Pair {
        private String key;
        private Float value;

        public Pair(String key, Float value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }
    }
}
