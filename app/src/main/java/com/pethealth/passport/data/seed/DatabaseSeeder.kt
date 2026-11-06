package com.pethealth.passport.data.seed

import com.pethealth.passport.data.local.AppDatabase
import com.pethealth.passport.data.local.entity.Comment
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.local.entity.Post
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.local.entity.WeightEntry
import androidx.room.withTransaction
import com.pethealth.passport.data.repository.SettingsRepository
import com.pethealth.passport.util.Dates
import java.util.Calendar

class DatabaseSeeder(
    private val database: AppDatabase,
    private val settings: SettingsRepository
) {
    suspend fun seedIfNeeded() {
        if (settings.seeded) return
        if (database.petDao().count() == 0) seed()
        settings.seeded = true
    }

    suspend fun reset() {
        database.withTransaction {
            val communityDao = database.communityDao()
            communityDao.deleteAllComments()
            communityDao.deleteAllPosts()
            val healthDao = database.healthDao()
            healthDao.deleteAllWeights()
            healthDao.deleteAllMedicalRecords()
            healthDao.deleteAllVaccinations()
            database.petDao().deleteAll()
        }
        settings.seeded = false
        seed()
        settings.seeded = true
    }

    private suspend fun seed() {
        val now = System.currentTimeMillis()
        val petDao = database.petDao()
        val healthDao = database.healthDao()
        val communityDao = database.communityDao()

        val miloId = petDao.insert(
            Pet(
                name = "Milo",
                species = "dog",
                breed = "Golden Retriever",
                gender = "male",
                birthDate = yearsAgo(3),
                weightKg = 12.5,
                color = "Vàng mật ong",
                microchip = "VN-982000123456789",
                ownerName = "Nguyễn Minh Anh",
                ownerPhone = "0912 345 678",
                notes = "Thân thiện, thích chơi bóng. Dị ứng với thức ăn nhiều cá."
            )
        )

        val lunaId = petDao.insert(
            Pet(
                name = "Luna",
                species = "cat",
                breed = "British Shorthair",
                gender = "female",
                birthDate = yearsAgo(2),
                weightKg = 4.3,
                color = "Xám xanh",
                microchip = "VN-982000987654321",
                ownerName = "Nguyễn Minh Anh",
                ownerPhone = "0912 345 678",
                notes = "Nhạy cảm với tiếng ồn, thích nằm cạnh cửa sổ."
            )
        )

        healthDao.insertVaccination(
            Vaccination(
                petId = miloId,
                name = "Vaccine dại",
                kind = Vaccination.KIND_VACCINE,
                date = Dates.addMonths(now, -11),
                nextDueDate = Dates.addDays(now, 20),
                clinic = "Bệnh viện Thú y Animal Doctors",
                vetName = "BS. Trần Quốc Huy",
                note = "Nhắc lại hằng năm."
            )
        )
        healthDao.insertVaccination(
            Vaccination(
                petId = miloId,
                name = "Vaccine 5 bệnh",
                kind = Vaccination.KIND_VACCINE,
                date = Dates.addMonths(now, -12),
                nextDueDate = Dates.addDays(now, 3),
                clinic = "Bệnh viện Thú y Animal Doctors",
                vetName = "BS. Trần Quốc Huy"
            )
        )
        healthDao.insertVaccination(
            Vaccination(
                petId = miloId,
                name = "Tẩy giun",
                kind = Vaccination.KIND_DEWORMER,
                date = Dates.addMonths(now, -3),
                nextDueDate = Dates.addDays(now, -2),
                clinic = "Phòng khám Thú y PetCare",
                note = "Đã quá hạn, cần tẩy lại."
            )
        )
        healthDao.insertVaccination(
            Vaccination(
                petId = lunaId,
                name = "Vaccine dại",
                kind = Vaccination.KIND_VACCINE,
                date = Dates.addMonths(now, -10),
                nextDueDate = Dates.addDays(now, 45),
                clinic = "Thú y Sài Gòn Vet",
                vetName = "BS. Lê Thu Hà"
            )
        )
        healthDao.insertVaccination(
            Vaccination(
                petId = lunaId,
                name = "Vaccine FVRCP",
                kind = Vaccination.KIND_VACCINE,
                date = Dates.addMonths(now, -11),
                nextDueDate = Dates.addDays(now, 10),
                clinic = "Thú y Sài Gòn Vet",
                vetName = "BS. Lê Thu Hà"
            )
        )
        healthDao.insertVaccination(
            Vaccination(
                petId = lunaId,
                name = "Tẩy giun",
                kind = Vaccination.KIND_DEWORMER,
                date = Dates.addMonths(now, -1),
                nextDueDate = Dates.addDays(now, 60),
                clinic = "Thú y Sài Gòn Vet"
            )
        )

        healthDao.insertMedicalRecord(
            MedicalRecord(
                petId = miloId,
                date = Dates.addDays(now, -25),
                clinic = "Bệnh viện Thú y Animal Doctors",
                vetName = "BS. Trần Quốc Huy",
                diagnosis = "Viêm da nhẹ do dị ứng thức ăn",
                prescription = "Kháng histamine 7 ngày, đổi sang thức ăn salmon-free",
                note = "Tái khám sau 2 tuần nếu còn ngứa."
            )
        )
        healthDao.insertMedicalRecord(
            MedicalRecord(
                petId = miloId,
                date = Dates.addDays(now, -120),
                clinic = "Phòng khám Thú y PetCare",
                vetName = "BS. Nguyễn Mai",
                diagnosis = "Khám sức khỏe định kỳ",
                prescription = "Không dùng thuốc",
                note = "Răng miệng tốt, cân nặng ổn định."
            )
        )
        healthDao.insertMedicalRecord(
            MedicalRecord(
                petId = lunaId,
                date = Dates.addDays(now, -40),
                clinic = "Thú y Sài Gòn Vet",
                vetName = "BS. Lê Thu Hà",
                diagnosis = "Viêm lợi nhẹ",
                prescription = "Gel răng miệng 10 ngày, tăng thức ăn ướt",
                note = "Kiểm tra lại sau 1 tháng."
            )
        )

        val miloWeights = listOf(11.4, 11.7, 12.0, 12.2, 12.4, 12.5)
        miloWeights.forEachIndexed { index, kg ->
            healthDao.insertWeight(
                WeightEntry(
                    petId = miloId,
                    date = Dates.addMonths(now, index - 5),
                    weightKg = kg,
                    note = if (index == miloWeights.lastIndex) "Cân gần nhất" else ""
                )
            )
        }
        val lunaWeights = listOf(3.7, 3.9, 4.0, 4.1, 4.2, 4.3)
        lunaWeights.forEachIndexed { index, kg ->
            healthDao.insertWeight(
                WeightEntry(
                    petId = lunaId,
                    date = Dates.addMonths(now, index - 5),
                    weightKg = kg
                )
            )
        }

        val post1 = communityDao.insertPost(
            Post(
                authorName = "Nguyễn Minh Anh",
                petName = "Milo",
                speciesTag = "dog",
                content = "Milo nhà mình vừa hoàn thành mũi vaccine dại hằng năm. Bé ngoan cực kỳ, chỉ run một chút rồi thôi 🐶 Các bạn nhớ kiểm tra lịch tiêm trong app nhé!",
                createdAt = now - 2 * 60 * 60 * 1000,
                likeCount = 24,
                commentCount = 2
            )
        )
        communityDao.insertComment(
            Comment(
                postId = post1,
                authorName = "Trần Bảo",
                content = "Milo dũng cảm quá! Bé nhà mình thì trốn dưới gầm ghế 😅",
                createdAt = now - 90 * 60 * 1000
            )
        )
        communityDao.insertComment(
            Comment(
                postId = post1,
                authorName = "Lê Thu Hà",
                content = "Nhớ giữ ấm và không tắm trong 7 ngày sau tiêm nha bạn.",
                createdAt = now - 60 * 60 * 1000
            )
        )

        val post2 = communityDao.insertPost(
            Post(
                authorName = "Trần Bảo",
                petName = "Bông",
                speciesTag = "cat",
                content = "Mèo nhà mình 3 tháng nay cứ đi tiểu nhiều lần mà chỉ ra vài giọt. Đi khám thì bác sĩ nói viêm đường tiết niệu. Mọi người cho mèo uống đủ nước và giữ khay cát sạch nhé, đừng chủ quan!",
                createdAt = now - 5 * 60 * 60 * 1000,
                likeCount = 41,
                commentCount = 1
            )
        )
        communityDao.insertComment(
            Comment(
                postId = post2,
                authorName = "Nguyễn Minh Anh",
                content = "Cảm ơn bạn đã chia sẻ. Mình cũng vừa đổi sang thức ăn ướt cho Luna.",
                createdAt = now - 4 * 60 * 60 * 1000
            )
        )

        communityDao.insertPost(
            Post(
                authorName = "Lê Thu Hà",
                petName = "Bác sĩ thú y",
                speciesTag = "other",
                content = "Nhắc nhẹ mùa nắng: không để thú cưng trong xe hơi dù chỉ 10 phút. Nhiệt độ trong xe có thể tăng gấp đôi rất nhanh. Hãy mang theo nước và bát uống cho bé.",
                createdAt = now - 26 * 60 * 60 * 1000,
                likeCount = 67,
                commentCount = 0
            )
        )

        communityDao.insertPost(
            Post(
                authorName = "Phạm Gia Hân",
                petName = "Cún",
                speciesTag = "dog",
                content = "Lần đầu dùng app, thích nhất là biểu đồ cân nặng. Trước giờ mình toàn ghi ra giấy rồi quên 😂 Giờ nhìn biểu đồ thấy Cún tăng cân đều quá.",
                createdAt = now - 2 * 24 * 60 * 60 * 1000,
                likeCount = 12,
                commentCount = 0
            )
        )
    }

    private fun yearsAgo(years: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -years)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
