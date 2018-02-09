package io.holyguacamole.bot.message

//@RunWith(SpringRunner::class)
//@DataMongoTest
//class RepositoryTest {
//
//    @Autowired lateinit var repository: AvocadoReceiptRepository
//
//    @Before
//    fun setUp() {
//        repository.saveAll(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)
//    }
//
//    @After
//    fun tearDown() {
//        repository.deleteAll()
//    }
//
//    @Test
//    fun `it finds AvocadoReceipts by eventId`() {
//        val avocadoReceipt = MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.first()
//
//        assert(repository.findByEventId(avocadoReceipt.eventId)).containsExactly(avocadoReceipt)
//    }
//}