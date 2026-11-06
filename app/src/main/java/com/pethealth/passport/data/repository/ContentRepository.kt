package com.pethealth.passport.data.repository

import com.pethealth.passport.data.model.Species

enum class ArticleCategory(val key: String) {
    NUTRITION("nutrition"),
    DISEASE("disease"),
    CARE("care");

    companion object {
        fun from(key: String?): ArticleCategory? = entries.firstOrNull { it.key == key }
    }
}

data class Article(
    val id: String,
    val category: ArticleCategory,
    val emoji: String,
    val titleVi: String,
    val titleEn: String,
    val bodyVi: String,
    val bodyEn: String
) {
    fun title(english: Boolean) = if (english) titleEn else titleVi
    fun body(english: Boolean) = if (english) bodyEn else bodyVi
}

data class QuizQuestion(
    val id: Int,
    val questionVi: String,
    val questionEn: String,
    val optionsVi: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explainVi: String,
    val explainEn: String
) {
    fun question(english: Boolean) = if (english) questionEn else questionVi
    fun options(english: Boolean) = if (english) optionsEn else optionsVi
    fun explanation(english: Boolean) = if (english) explainEn else explainVi
}

data class Clinic(
    val name: String,
    val address: String,
    val phone: String,
    val hoursVi: String,
    val hoursEn: String,
    val latitude: Double,
    val longitude: Double
) {
    fun hours(english: Boolean) = if (english) hoursEn else hoursVi
}

class ContentRepository(private val settings: SettingsRepository) {

    fun isEnglish(): Boolean = settings.isEnglish()

    fun speciesLabel(key: String): String = Species.label(key, isEnglish())

    val articles: List<Article> = listOf(
        Article(
            id = "nutrition-age",
            category = ArticleCategory.NUTRITION,
            emoji = "🍗",
            titleVi = "Dinh dưỡng theo từng độ tuổi",
            titleEn = "Nutrition at every life stage",
            bodyVi = "Chó mèo dưới 1 tuổi cần thức ăn giàu đạm và năng lượng để phát triển xương, cơ. Từ 1–7 tuổi dùng thức ăn duy trì, chia 2 bữa/ngày. Trên 7 tuổi nên chọn công thức ít calo, giàu chất xơ và bổ sung omega-3 để bảo vệ khớp, thận.\n\n" +
                "Một mẹo đơn giản: nhìn được eo và sờ thấy xương sườn nhẹ nhàng là thể trạng lý tưởng. Nếu không thấy eo, hãy giảm 10% khẩu phần và tăng vận động từ từ.",
            bodyEn = "Puppies and kittens under 1 year old need protein- and energy-dense food for bone and muscle growth. From 1–7 years, switch to maintenance food and split it into two meals a day. After 7, choose a lower-calorie, fibre-rich formula with omega-3 to protect joints and kidneys.\n\n" +
                "A simple rule: you should see a waist and feel the ribs easily. If the waist is gone, cut the portion by 10% and increase exercise gradually."
        ),
        Article(
            id = "water-portions",
            category = ArticleCategory.NUTRITION,
            emoji = "💧",
            titleVi = "Nước sạch và khẩu phần chuẩn",
            titleEn = "Fresh water and the right portion",
            bodyVi = "Luôn để nước sạch trong bát nặng, thay 2 lần mỗi ngày. Lượng nước tham khảo: 50–60 ml cho mỗi kg cân nặng/ngày, tăng khi trời nóng hoặc vận động nhiều.\n\n" +
                "Khẩu phần nên tính theo cân nặng và mức vận động. Đừng cho ăn tự do cả ngày với chó mèo trong nhà — đây là nguyên nhân phổ biến nhất gây béo phì. Hãy cân thức ăn bằng cốc định lượng.",
            bodyEn = "Keep fresh water in a heavy bowl and change it twice a day. A useful guide is 50–60 ml per kg of body weight per day, more in hot weather or after activity.\n\n" +
                "Calculate portions from weight and activity level. Avoid free-feeding indoor pets — it is the most common cause of obesity. Use a measuring cup to weigh food."
        ),
        Article(
            id = "toxic-foods",
            category = ArticleCategory.NUTRITION,
            emoji = "🚫",
            titleVi = "Thực phẩm cấm với chó mèo",
            titleEn = "Foods that are toxic to pets",
            bodyVi = "Sô-cô-la, nho/nho khô, hành, tỏi, hẹ, xylitol (chất tạo ngọt trong kẹo cao su), caffeine và rượu đều có thể gây ngộ độc, thậm chí tử vong.\n\n" +
                "Xương nấu chín dễ vỡ thành mảnh sắc. Cá sống chứa enzym phá hủy vitamin B1. Sữa bò gây tiêu chảy ở nhiều mèo. Nếu vật nuôi đã ăn phải, hãy gọi bác sĩ thú y ngay và mang theo nhãn sản phẩm.",
            bodyEn = "Chocolate, grapes/raisins, onion, garlic, chives, xylitol (a sweetener in gum), caffeine and alcohol can cause poisoning or even death.\n\n" +
                "Cooked bones splinter easily. Raw fish contains an enzyme that destroys vitamin B1. Cow's milk causes diarrhoea in many cats. If your pet has eaten any of these, call a vet immediately and bring the product label."
        ),
        Article(
            id = "food-transition",
            category = ArticleCategory.NUTRITION,
            emoji = "🥣",
            titleVi = "Đổi thức ăn đúng cách trong 7 ngày",
            titleEn = "Switch food safely over 7 days",
            bodyVi = "Đột ngột đổi thức ăn dễ gây nôn, tiêu chảy. Hãy trộn theo tỉ lệ: ngày 1–2 (25% mới), ngày 3–4 (50%), ngày 5–6 (75%), ngày 7 (100%).\n\n" +
                "Nếu phân lỏng kéo dài hơn 2 ngày hoặc vật nuôi bỏ ăn, hãy quay lại tỉ lệ cũ và hỏi bác sĩ thú y.",
            bodyEn = "Switching food abruptly often causes vomiting or diarrhoea. Mix gradually: days 1–2 (25% new food), days 3–4 (50%), days 5–6 (75%), day 7 (100%).\n\n" +
                "If diarrhoea lasts more than two days or your pet stops eating, go back to the previous ratio and ask your vet."
        ),
        Article(
            id = "parvo",
            category = ArticleCategory.DISEASE,
            emoji = "🦠",
            titleVi = "Parvo ở chó: nguy hiểm và cấp tính",
            titleEn = "Canine parvovirus: acute and dangerous",
            bodyVi = "Parvo gây nôn, tiêu chảy ra máu, mất nước và suy nhược nhanh, đặc biệt ở chó con chưa tiêm phòng. Virus tồn tại lâu trong môi trường và lây qua phân.\n\n" +
                "Phòng bệnh bằng vaccine đúng lịch, không cho chó con ra nơi công cộng khi chưa tiêm đủ mũi. Nếu thấy nôn nhiều, phân có máu hoặc li bì, cần đưa đi khám NGAY — không tự truyền nước tại nhà.",
            bodyEn = "Parvovirus causes vomiting, bloody diarrhoea, dehydration and rapid collapse, especially in unvaccinated puppies. The virus survives a long time in the environment and spreads through faeces.\n\n" +
                "Prevent it with on-schedule vaccination and keep unvaccinated puppies away from public areas. Seek urgent veterinary care for repeated vomiting, bloody stool or lethargy — do not attempt home fluid therapy."
        ),
        Article(
            id = "rabies",
            category = ArticleCategory.DISEASE,
            emoji = "💉",
            titleVi = "Bệnh dại: tiêm phòng là bắt buộc",
            titleEn = "Rabies: vaccination is essential",
            bodyVi = "Bệnh dại do virus tấn công hệ thần kinh, gần như 100% tử vong khi đã phát bệnh và có thể lây sang người. Chó, mèo cần tiêm nhắc hằng năm.\n\n" +
                "Sau khi bị động vật khác cắn, hãy rửa vết thương bằng xà phòng dưới vòi nước 15 phút, đến cơ sở y tế và theo dõi con vật. Đừng chờ triệu chứng xuất hiện.",
            bodyEn = "Rabies attacks the nervous system and is almost always fatal once symptoms appear; it can spread to humans. Dogs and cats need an annual booster.\n\n" +
                "After an animal bite, wash the wound with soap under running water for 15 minutes, go to a medical facility and monitor the animal. Do not wait for symptoms."
        ),
        Article(
            id = "worms",
            category = ArticleCategory.DISEASE,
            emoji = "🪱",
            titleVi = "Tẩy giun định kỳ 3 tháng/lần",
            titleEn = "Deworm every three months",
            bodyVi = "Giun tròn, giun móc, giun dẹp có thể gây thiếu máu, tiêu chảy, chậm lớn và lây sang người. Chó mèo trưởng thành nên tẩy giun mỗi 3 tháng, chó con mỗi 2–4 tuần theo chỉ định.\n\n" +
                "Thuốc tẩy giun cần chọn đúng cân nặng và loài. Không dùng thuốc cho mèo với chó và ngược lại. Vệ sinh khay cát, rửa tay sau khi dọn phân.",
            bodyEn = "Roundworms, hookworms and tapeworms can cause anaemia, diarrhoea and stunted growth, and some spread to humans. Adult pets should be dewormed every three months; puppies every 2–4 weeks as advised.\n\n" +
                "Choose the correct product for the species and weight. Never give dog products to cats or vice versa. Clean litter trays and wash hands after handling faeces."
        ),
        Article(
            id = "flutd",
            category = ArticleCategory.DISEASE,
            emoji = "🐱",
            titleVi = "Viêm đường tiết niệu ở mèo",
            titleEn = "Feline lower urinary tract disease",
            bodyVi = "Mèo đực có thể bị tắc niệu đạo — tình trạng cấp cứu. Dấu hiệu: đi tiểu nhiều lần, chỉ ra vài giọt, kêu khi đi tiểu, liếm vùng kín, bỏ ăn.\n\n" +
                "Tăng nước uống bằng máy lọc nước, cho ăn thức ăn ướt, giữ khay cát sạch và giảm stress. Nếu mèo không đi tiểu được trong 12 giờ, đưa đi cấp cứu ngay.",
            bodyEn = "Male cats can develop urethral obstruction — a medical emergency. Signs include frequent trips to the litter box, tiny urine volumes, crying while urinating, over-grooming and refusing food.\n\n" +
                "Increase water intake with a fountain, feed wet food, keep litter clean and reduce stress. If a cat cannot urinate for 12 hours, seek emergency care."
        ),
        Article(
            id = "vaccine-schedule",
            category = ArticleCategory.CARE,
            emoji = "📅",
            titleVi = "Lịch tiêm phòng định kỳ",
            titleEn = "Routine vaccination schedule",
            bodyVi = "Chó con: mũi 1 lúc 6–8 tuần, mũi 2 sau 3–4 tuần, mũi 3 lúc 14–16 tuần, sau đó nhắc hằng năm. Mèo con tương tự từ 8 tuần.\n\n" +
                "Vaccine dại thường nhắc mỗi 12 tháng. Hãy lưu ngày tiêm và ngày nhắc vào ứng dụng để không bỏ lỡ. Vắc-xin không bảo vệ tuyệt đối 100% nhưng giảm mạnh nguy cơ và mức độ bệnh.",
            bodyEn = "Puppies: first dose at 6–8 weeks, second 3–4 weeks later, third at 14–16 weeks, then annual boosters. Kittens follow a similar schedule from 8 weeks.\n\n" +
                "Rabies boosters are usually annual. Record the date and the next due date in the app so you never miss one. Vaccines are not 100% protective but greatly reduce risk and severity."
        ),
        Article(
            id = "grooming",
            category = ArticleCategory.CARE,
            emoji = "🛁",
            titleVi = "Tắm và chải lông đúng cách",
            titleEn = "Bathing and grooming the right way",
            bodyVi = "Chó khỏe mạnh thường tắm 3–4 tuần/lần, mèo 6–8 tuần/lần hoặc khi bẩn. Dùng sản phẩm dành riêng cho thú cưng, nước ấm vừa, sấy khô kỹ nhất là vùng tai và bàn chân.\n\n" +
                "Chải lông 2–3 lần/tuần giúp giảm búi lông và phát hiện vết thương, ký sinh trùng sớm. Không tắm cho chó mèo ngay sau khi tiêm vaccine trong 7 ngày.",
            bodyEn = "Healthy dogs usually need a bath every 3–4 weeks; cats every 6–8 weeks or when dirty. Use pet-specific shampoo, lukewarm water and dry thoroughly, especially ears and paws.\n\n" +
                "Brush 2–3 times a week to reduce matting and spot wounds or parasites early. Do not bathe your pet within 7 days of vaccination."
        ),
        Article(
            id = "ideal-weight",
            category = ArticleCategory.CARE,
            emoji = "⚖️",
            titleVi = "Cân nặng lý tưởng và cách theo dõi",
            titleEn = "Ideal weight and how to track it",
            bodyVi = "Cân mỗi 2–4 tuần vào cùng thời điểm (trước bữa ăn sáng) và ghi lại. Biểu đồ giúp phát hiện sụt cân bất thường — dấu hiệu sớm của bệnh răng miệng, thận, tiểu đường.\n\n" +
                "Thân hình lý tưởng: nhìn từ trên thấy eo rõ, sờ thấy xương sườn nhưng không lộ rõ. Giảm 1–2% cân nặng mỗi tuần là mức an toàn khi cần giảm cân.",
            bodyEn = "Weigh your pet every 2–4 weeks at the same time (before breakfast) and log it. The chart reveals unexpected weight loss — an early sign of dental, kidney or diabetic disease.\n\n" +
                "Ideal shape: a visible waist from above and ribs felt easily but not prominent. Losing 1–2% of body weight per week is a safe rate when dieting."
        ),
        Article(
            id = "emergency-signs",
            category = ArticleCategory.CARE,
            emoji = "🚨",
            titleVi = "Dấu hiệu cần đi khám ngay",
            titleEn = "Signs that need urgent care",
            bodyVi = "Đưa thú cưng đến bác sĩ ngay nếu: khó thở, nôn ra máu, tiêu chảy ra máu, co giật, không đi tiểu, bụng chướng cấp, li bì không phản ứng, thân nhiệt dưới 37°C hoặc trên 40°C.\n\n" +
                "Trước khi đi, ghi lại thời gian khởi phát, triệu chứng và thuốc đã dùng. Không tự cho uống thuốc người, đặc biệt là paracetamol và ibuprofen — có thể gây tử vong.",
            bodyEn = "Seek urgent care for: difficulty breathing, vomiting blood, bloody diarrhoea, seizures, inability to urinate, a hard distended abdomen, unresponsiveness, temperature below 37°C or above 40°C.\n\n" +
                "Before you go, note the onset time, symptoms and any medication given. Never give human medicine, especially paracetamol or ibuprofen, which can be fatal."
        )
    )

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = 1,
            questionVi = "Vaccine bệnh dại cho chó mèo thường được nhắc lại sau bao lâu?",
            questionEn = "How often is the rabies vaccine usually boosted?",
            optionsVi = listOf("3 tháng", "6 tháng", "12 tháng", "5 năm"),
            optionsEn = listOf("3 months", "6 months", "12 months", "5 years"),
            correctIndex = 2,
            explainVi = "Vaccine dại thường được nhắc hằng năm (12 tháng), tùy loại vắc-xin và chỉ định của bác sĩ.",
            explainEn = "Rabies vaccines are usually boosted annually (12 months), depending on the product and your vet's advice."
        ),
        QuizQuestion(
            id = 2,
            questionVi = "Chó mèo trưởng thành nên tẩy giun định kỳ bao lâu một lần?",
            questionEn = "How often should adult pets be dewormed?",
            optionsVi = listOf("1 tháng", "3 tháng", "1 năm", "Chỉ khi thấy giun"),
            optionsEn = listOf("Every month", "Every 3 months", "Once a year", "Only when worms are visible"),
            correctIndex = 1,
            explainVi = "Tẩy giun mỗi 3 tháng giúp kiểm soát giun tròn, giun móc và giảm nguy cơ lây sang người.",
            explainEn = "Deworming every 3 months controls roundworms and hookworms and lowers the risk to humans."
        ),
        QuizQuestion(
            id = 3,
            questionVi = "Thực phẩm nào gây ngộ độc nguy hiểm cho chó?",
            questionEn = "Which food is dangerously toxic to dogs?",
            optionsVi = listOf("Cà rốt luộc", "Sô-cô-la", "Ức gà", "Gạo trắng"),
            optionsEn = listOf("Boiled carrot", "Chocolate", "Chicken breast", "White rice"),
            correctIndex = 1,
            explainVi = "Sô-cô-la chứa theobromine, có thể gây nôn, tim đập nhanh, co giật và tử vong.",
            explainEn = "Chocolate contains theobromine, which can cause vomiting, a racing heart, seizures and death."
        ),
        QuizQuestion(
            id = 4,
            questionVi = "Thân nhiệt bình thường của mèo vào khoảng nào?",
            questionEn = "What is a normal body temperature for a cat?",
            optionsVi = listOf("35–36°C", "38–39,2°C", "40–41°C", "36–37°C"),
            optionsEn = listOf("35–36°C", "38–39.2°C", "40–41°C", "36–37°C"),
            correctIndex = 1,
            explainVi = "Mèo khỏe mạnh thường có thân nhiệt 38–39,2°C. Trên 39,5°C được coi là sốt.",
            explainEn = "Healthy cats usually sit between 38 and 39.2°C. Above 39.5°C is considered a fever."
        ),
        QuizQuestion(
            id = 5,
            questionVi = "Dấu hiệu nào KHÔNG điển hình của bệnh Parvo ở chó?",
            questionEn = "Which sign is NOT typical of canine parvovirus?",
            optionsVi = listOf("Nôn nhiều", "Tiêu chảy ra máu", "Bỏ ăn, li bì", "Lông mọc dài bất thường"),
            optionsEn = listOf("Repeated vomiting", "Bloody diarrhoea", "Loss of appetite and lethargy", "Abnormally fast hair growth"),
            correctIndex = 3,
            explainVi = "Parvo gây nôn, tiêu chảy máu, mất nước và suy nhược. Lông mọc dài không phải triệu chứng.",
            explainEn = "Parvo causes vomiting, bloody diarrhoea, dehydration and collapse. Fast hair growth is not a symptom."
        ),
        QuizQuestion(
            id = 6,
            questionVi = "Khi mèo đực không đi tiểu được 12 giờ, bạn nên làm gì?",
            questionEn = "What should you do if a male cat cannot urinate for 12 hours?",
            optionsVi = listOf("Chờ thêm 1 ngày", "Cho uống nhiều nước", "Đưa đi cấp cứu ngay", "Massage bụng"),
            optionsEn = listOf("Wait another day", "Give lots of water", "Seek emergency care now", "Massage the belly"),
            correctIndex = 2,
            explainVi = "Tắc niệu đạo có thể gây suy thận và tử vong trong 24–48 giờ. Đây là cấp cứu.",
            explainEn = "Urethral obstruction can cause kidney failure and death within 24–48 hours. It is an emergency."
        ),
        QuizQuestion(
            id = 7,
            questionVi = "Cách giảm cân an toàn cho thú cưng béo phì?",
            questionEn = "How do you help an overweight pet lose weight safely?",
            optionsVi = listOf("Bỏ hẳn bữa sáng", "Giảm 1–2% cân nặng mỗi tuần", "Cho ăn thật ít trong 3 ngày", "Chỉ cho ăn đồ ăn vặt"),
            optionsEn = listOf("Skip breakfast entirely", "Lose 1–2% of body weight per week", "Feed very little for 3 days", "Feed only treats"),
            correctIndex = 1,
            explainVi = "Giảm cân từ từ 1–2%/tuần, kết hợp khẩu phần đo lường và vận động, an toàn hơn nhịn ăn.",
            explainEn = "A gradual loss of 1–2% per week, with measured portions and exercise, is safer than fasting."
        )
    )

    val clinics: List<Clinic> = listOf(
        Clinic(
            "Phòng khám Thú y PetCare",
            "123 Nguyễn Văn Cừ, Long Biên, Hà Nội",
            "024 3876 5432",
            "08:00 – 20:00 hằng ngày",
            "08:00 – 20:00 daily",
            21.0468,
            105.8789
        ),
        Clinic(
            "Bệnh viện Thú y Animal Doctors",
            "56 Trần Duy Hưng, Cầu Giấy, Hà Nội",
            "024 3786 1234",
            "Cấp cứu 24/7",
            "24/7 emergency",
            21.0132,
            105.7945
        ),
        Clinic(
            "Thú y Sài Gòn Vet",
            "88 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh",
            "028 3822 5566",
            "08:30 – 21:00 hằng ngày",
            "08:30 – 21:00 daily",
            10.7832,
            106.6971
        ),
        Clinic(
            "Pet Health Clinic",
            "201 Lê Văn Sỹ, Quận 3, TP. Hồ Chí Minh",
            "028 3931 7788",
            "08:00 – 19:00, nghỉ Chủ nhật",
            "08:00 – 19:00, closed Sunday",
            10.7897,
            106.6791
        ),
        Clinic(
            "Thú y Đà Nẵng",
            "45 Nguyễn Văn Linh, Hải Châu, Đà Nẵng",
            "0236 365 4321",
            "08:00 – 20:00 hằng ngày",
            "08:00 – 20:00 daily",
            16.0602,
            108.2221
        )
    )

    fun articlesFor(category: ArticleCategory?): List<Article> =
        if (category == null) articles else articles.filter { it.category == category }
}
