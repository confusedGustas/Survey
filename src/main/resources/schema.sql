DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS surveys CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS choices CASCADE;
DROP TABLE IF EXISTS answers CASCADE;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS surveys (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    survey_id INTEGER NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    question_size INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS choices (
    id SERIAL PRIMARY KEY,
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    choice_text VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS answers (
    id SERIAL PRIMARY KEY,
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    choice_id INTEGER REFERENCES choices(id) ON DELETE SET NULL,
    is_public BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email, password, role) 
VALUES ('test', 'testadmin@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, email, password, role) VALUES
('user1', 'user1@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user2', 'user2@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user3', 'user3@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user4', 'user4@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user5', 'user5@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('researcher1', 'researcher1@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('researcher2', 'researcher2@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('moderator1', 'moderator1@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('jane_doe', 'jane@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('john_smith', 'john@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO surveys (title, description, created_by) VALUES
('Customer Satisfaction Survey', 'Help us improve our customer service', 1),
('Employee Engagement Survey', 'Annual employee satisfaction assessment', 1),
('Product Feedback Survey', 'Share your thoughts on our new product line', 7),
('Website Usability Survey', 'Evaluate our website redesign', 8),
('Market Research Survey', 'Help us understand market trends', 7),
('Event Feedback Survey', 'Tell us about your experience at our annual conference', 1),
('Healthcare Experience Survey', 'Rate your recent visit to our medical facility', 7),
('Software Usability Survey', 'Evaluate our new software interface', 8),
('Educational Program Evaluation', 'Feedback on our training programs', 1),
('Restaurant Dining Experience', 'Tell us about your dining experience', 7),
('Travel Preferences Survey', 'Help us design better travel packages', 8),
('Technology Usage Survey', 'How do you use technology in daily life', 7),
('Social Media Habits Survey', 'Share your social media usage patterns', 8),
('Financial Services Feedback', 'Evaluate our banking services', 1),
('Remote Work Experience', 'Share your work-from-home experience', 7),
('Environmental Awareness Survey', 'Assess community environmental concerns', 8),
('Fitness and Wellness Survey', 'Share your fitness goals and habits', 1),
('Gaming Preferences Survey', 'Tell us about your gaming habits', 7),
('Reading Habits Survey', 'Share your reading preferences and habits', 8),
('Shopping Experience Survey', 'Evaluate your online shopping experience', 1);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(11, 'How often do you travel?', 'MULTIPLE', NULL),
(11, 'What type of accommodation do you prefer?', 'MULTIPLE', NULL), 
(11, 'What is your favorite travel destination and why?', 'TEXT', NULL),
(11, 'How important is sustainable/eco-friendly travel to you?', 'SINGLE', 5),
(11, 'What is your typical travel budget per trip?', 'MULTIPLE', NULL);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(12, 'How much time do you spend using digital devices daily?', 'MULTIPLE', NULL),
(12, 'Which devices do you use regularly?', 'MULTIPLE', NULL),
(12, 'What challenges do you face with technology?', 'TEXT', NULL),
(12, 'How comfortable are you adapting to new technologies?', 'SINGLE', 5),
(12, 'Which emerging technologies are you most excited about?', 'MULTIPLE', NULL);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(13, 'Which social media platforms do you use regularly?', 'MULTIPLE', NULL),
(13, 'How much time do you spend on social media daily?', 'MULTIPLE', NULL),
(13, 'What type of content do you engage with most?', 'MULTIPLE', NULL),
(13, 'How has social media affected your daily life?', 'TEXT', NULL),
(13, 'Do you use social media for professional networking?', 'SINGLE', NULL);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(14, 'How satisfied are you with your current financial services?', 'SINGLE', 5),
(14, 'Which financial services do you currently use?', 'MULTIPLE', NULL),
(14, 'What improvements would you like to see in financial services?', 'TEXT', NULL),
(14, 'How important is online/mobile banking to you?', 'SINGLE', 5),
(14, 'How often do you visit a physical bank branch?', 'MULTIPLE', NULL);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(15, 'How productive are you when working remotely?', 'SINGLE', 5),
(15, 'What challenges do you face when working remotely?', 'MULTIPLE', NULL),
(15, 'What could improve your remote work experience?', 'TEXT', NULL),
(15, 'What work arrangement do you prefer?', 'MULTIPLE', NULL),
(15, 'How many days per week do you work remotely?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(1, 'How satisfied are you with our customer service?', 'SINGLE', 5),
(1, 'How likely are you to recommend our company to others?', 'SINGLE', 10),
(1, 'What aspects of our service could be improved?', 'TEXT', NULL),
(1, 'How would you rate the response time of our support team?', 'SINGLE', 5),
(1, 'Which of our departments did you interact with?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(2, 'How satisfied are you with your current role?', 'SINGLE', 5),
(2, 'Do you feel your work is valued by management?', 'SINGLE', 5),
(2, 'What would increase your job satisfaction?', 'TEXT', NULL),
(2, 'How would you rate the company culture?', 'SINGLE', 5),
(2, 'Which company benefits do you value most?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(3, 'How would you rate the product quality?', 'SINGLE', 5),
(3, 'Does the product meet your expectations?', 'SINGLE', 5),
(3, 'What features would you like to see added?', 'TEXT', NULL),
(3, 'How would you rate the value for money?', 'SINGLE', 5),
(3, 'Which product feature do you use most frequently?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(4, 'How easy is it to navigate our website?', 'SINGLE', 5),
(4, 'Did you find what you were looking for?', 'SINGLE', NULL),
(4, 'What aspects of the website could be improved?', 'TEXT', NULL),
(4, 'How would you rate the website design?', 'SINGLE', 5),
(4, 'Which website features do you find most useful?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(5, 'How often do you purchase products in this category?', 'MULTIPLE', NULL),
(5, 'What factors influence your purchasing decisions?', 'MULTIPLE', NULL),
(5, 'What improvements would you suggest for this product category?', 'TEXT', NULL),
(5, 'How much would you be willing to pay for this product?', 'MULTIPLE', NULL),
(5, 'Which brands do you currently use?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(6, 'How would you rate the overall event?', 'SINGLE', 5),
(6, 'Were the speakers engaging?', 'SINGLE', 5),
(6, 'What topics would you like to see at future events?', 'TEXT', NULL),
(6, 'How was the venue?', 'SINGLE', 5),
(6, 'Which session did you find most valuable?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(7, 'How would you rate your overall experience?', 'SINGLE', 5),
(7, 'Was the staff courteous and helpful?', 'SINGLE', 5),
(7, 'Were your medical needs adequately addressed?', 'SINGLE', 5),
(7, 'How long did you wait before seeing a doctor?', 'MULTIPLE', NULL),
(7, 'Would you return to this facility for future care?', 'SINGLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(8, 'How intuitive was the software to use?', 'SINGLE', 5),
(8, 'Did you encounter any bugs or errors?', 'SINGLE', NULL),
(8, 'What features would you like to see added?', 'TEXT', NULL),
(8, 'How would you rate the software performance?', 'SINGLE', 5),
(8, 'Which software feature do you use most often?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(9, 'How would you rate the quality of instruction?', 'SINGLE', 5),
(9, 'Was the course content relevant to your needs?', 'SINGLE', 5),
(9, 'What additional topics would you like to see covered?', 'TEXT', NULL),
(9, 'How would you rate the course materials?', 'SINGLE', 5),
(9, 'Which learning format do you prefer?', 'MULTIPLE', NULL);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(10, 'How would you rate the food quality?', 'SINGLE', 5),
(10, 'Was the service prompt and courteous?', 'SINGLE', 5),
(10, 'What dish would you recommend adding to our menu?', 'TEXT', NULL),
(10, 'How would you rate the restaurant ambiance?', 'SINGLE', 5),
(10, 'What type of cuisine do you prefer?', 'MULTIPLE', NULL);


INSERT INTO choices (question_id, choice_text) VALUES
(51, 'Several times a year'),
(51, 'Once a year'),
(51, 'Every few years'),
(51, 'Rarely'),
(51, 'Never'),

(52, 'Luxury hotels'),
(52, 'Budget hotels'),
(52, 'Vacation rentals'),
(52, 'Hostels'),
(52, 'Camping'),

(54, 'Not at all'),
(54, 'Slightly'),
(54, 'Moderately'),
(54, 'Very'),
(54, 'Extremely'),

(55, 'Less than $1000'),
(55, '$1000-$3000'),
(55, '$3000-$5000'),
(55, '$5000-$10000'),
(55, 'More than $10000');


INSERT INTO choices (question_id, choice_text) VALUES
(56, 'Less than 1 hour'),
(56, '1-3 hours'),
(56, '4-6 hours'),
(56, '7-10 hours'),
(56, 'More than 10 hours'),

(57, 'Smartphone'),
(57, 'Laptop/Desktop'),
(57, 'Tablet'),
(57, 'Smart TV'),
(57, 'Wearable Tech'),

(60, 'Artificial Intelligence'),
(60, 'Virtual Reality'),
(60, 'Smart Home Technology'),
(60, 'Renewable Energy Tech'),
(60, 'Self-driving Vehicles');

INSERT INTO choices (question_id, choice_text) VALUES
(5, 'Sales'),
(5, 'Customer Support'),
(5, 'Technical Support'),
(5, 'Billing Department'),
(5, 'Product Development');

INSERT INTO choices (question_id, choice_text) VALUES
(10, 'Health Insurance'),
(10, 'Retirement Plan'),
(10, 'Paid Time Off'),
(10, 'Flexible Work Hours'),
(10, 'Professional Development');

INSERT INTO choices (question_id, choice_text) VALUES
(15, 'User Interface'),
(15, 'Performance'),
(15, 'Reliability'),
(15, 'Customization Options'),
(15, 'Integration with Other Tools');

INSERT INTO choices (question_id, choice_text) VALUES
(20, 'Search Function'),
(20, 'Product Filtering'),
(20, 'User Reviews'),
(20, 'Shopping Cart'),
(20, 'Mobile Compatibility');

INSERT INTO choices (question_id, choice_text) VALUES
(21, 'Daily'),
(21, 'Weekly'),
(21, 'Monthly'),
(21, 'Quarterly'),
(21, 'Rarely');

INSERT INTO choices (question_id, choice_text) VALUES
(22, 'Price'),
(22, 'Quality'),
(22, 'Brand Reputation'),
(22, 'Customer Reviews'),
(22, 'Environmental Impact');

INSERT INTO choices (question_id, choice_text) VALUES
(24, 'Less than $50'),
(24, '$50-$100'),
(24, '$100-$200'),
(24, '$200-$500'),
(24, 'More than $500');

INSERT INTO choices (question_id, choice_text) VALUES
(25, 'Brand A'),
(25, 'Brand B'),
(25, 'Brand C'),
(25, 'Brand D'),
(25, 'Other');

INSERT INTO choices (question_id, choice_text) VALUES
(30, 'Keynote Presentation'),
(30, 'Panel Discussion'),
(30, 'Workshop A'),
(30, 'Workshop B'),
(30, 'Networking Session');

INSERT INTO choices (question_id, choice_text) VALUES
(34, 'Less than 15 minutes'),
(34, '15-30 minutes'),
(34, '30-60 minutes'),
(34, '1-2 hours'),
(34, 'More than 2 hours');

INSERT INTO choices (question_id, choice_text) VALUES
(40, 'Document Creation'),
(40, 'Data Analysis'),
(40, 'Collaboration Tools'),
(40, 'Project Management'),
(40, 'Communication Features');

INSERT INTO choices (question_id, choice_text) VALUES
(45, 'In-person Classroom'),
(45, 'Live Virtual Sessions'),
(45, 'Self-paced Online'),
(45, 'Blended Learning'),
(45, 'One-on-one Mentoring');

INSERT INTO choices (question_id, choice_text) VALUES
(50, 'Italian'),
(50, 'Asian Fusion'),
(50, 'Mexican'),
(50, 'Mediterranean'),
(50, 'American');

INSERT INTO choices (question_id, choice_text) VALUES
(5, 'Product Returns'),
(10, 'Company Events'),
(15, 'Security Features'),
(20, 'Product Comparisons'),
(21, 'Bi-weekly'),
(22, 'Warranty'),
(24, '$500-$1000'),
(25, 'Brand E'),
(30, 'Closing Remarks'),
(34, 'Scheduled Appointment');

INSERT INTO choices (question_id, choice_text) VALUES
(1, 'Very Unsatisfied'),
(1, 'Unsatisfied'),
(1, 'Neutral'),
(1, 'Satisfied'),
(1, 'Very Satisfied');

INSERT INTO choices (question_id, choice_text) VALUES
(2, '1 - Not at all likely'),
(2, '2'),
(2, '3'),
(2, '4'),
(2, '5 - Neutral'),
(2, '6'),
(2, '7'),
(2, '8'),
(2, '9'),
(2, '10 - Extremely likely');

INSERT INTO choices (question_id, choice_text) VALUES
(4, 'Very Poor'),
(4, 'Poor'),
(4, 'Average'),
(4, 'Good'),
(4, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(6, 'Very Unsatisfied'),
(6, 'Unsatisfied'),
(6, 'Neutral'),
(6, 'Satisfied'),
(6, 'Very Satisfied');

INSERT INTO choices (question_id, choice_text) VALUES
(7, 'Not at all'),
(7, 'Rarely'),
(7, 'Sometimes'),
(7, 'Often'),
(7, 'Always');

INSERT INTO choices (question_id, choice_text) VALUES
(9, 'Very Poor'),
(9, 'Poor'),
(9, 'Average'),
(9, 'Good'),
(9, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(11, 'Very Poor'),
(11, 'Poor'),
(11, 'Average'),
(11, 'Good'),
(11, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(12, 'Not at all'),
(12, 'Somewhat'),
(12, 'Mostly'),
(12, 'Completely'),
(12, 'Exceeds expectations');

INSERT INTO choices (question_id, choice_text) VALUES
(14, 'Very Poor'),
(14, 'Poor'),
(14, 'Fair'),
(14, 'Good'),
(14, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(16, 'Very Difficult'),
(16, 'Difficult'),
(16, 'Neutral'),
(16, 'Easy'),
(16, 'Very Easy');

INSERT INTO choices (question_id, choice_text) VALUES
(17, 'No'),
(17, 'Partially'),
(17, 'Yes');

INSERT INTO choices (question_id, choice_text) VALUES
(19, 'Very Poor'),
(19, 'Poor'),
(19, 'Average'),
(19, 'Good'),
(19, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(26, 'Poor'),
(26, 'Below Average'),
(26, 'Average'),
(26, 'Good'),
(26, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(27, 'Not at all'),
(27, 'Somewhat'),
(27, 'Moderately'),
(27, 'Very'),
(27, 'Extremely');

INSERT INTO choices (question_id, choice_text) VALUES
(29, 'Poor'),
(29, 'Below Average'),
(29, 'Average'),
(29, 'Good'),
(29, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(31, 'Poor'),
(31, 'Below Average'),
(31, 'Average'),
(31, 'Good'),
(31, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(32, 'No'),
(32, 'Yes, minor issues'),
(32, 'Yes, major issues');

INSERT INTO choices (question_id, choice_text) VALUES
(35, 'No'),
(35, 'Maybe'),
(35, 'Yes');

INSERT INTO choices (question_id, choice_text) VALUES
(36, 'Very Poor'),
(36, 'Poor'),
(36, 'Average'),
(36, 'Good'),
(36, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(37, 'Not at all'),
(37, 'Somewhat'),
(37, 'Mostly'),
(37, 'Completely');

INSERT INTO choices (question_id, choice_text) VALUES
(39, 'Poor'),
(39, 'Below Average'),
(39, 'Average'),
(39, 'Good'),
(39, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(41, 'Very Poor'),
(41, 'Poor'),
(41, 'Average'),
(41, 'Good'),
(41, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(42, 'No'),
(42, 'Sometimes'),
(42, 'Yes, consistently');

INSERT INTO choices (question_id, choice_text) VALUES
(44, 'Poor'),
(44, 'Below Average'),
(44, 'Average'),
(44, 'Good'),
(44, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(46, 'Very Poor'),
(46, 'Poor'),
(46, 'Average'),
(46, 'Good'),
(46, 'Excellent');

INSERT INTO choices (question_id, choice_text) VALUES
(47, 'No'),
(47, 'Somewhat'),
(47, 'Yes');

INSERT INTO choices (question_id, choice_text) VALUES
(49, 'Not at all'),
(49, 'Slightly'),
(49, 'Moderately'),
(49, 'Very'),
(49, 'Extremely');

INSERT INTO choices (question_id, choice_text) VALUES
(54, 'Not at all'),
(54, 'Slightly'),
(54, 'Moderately'),
(54, 'Very'),
(54, 'Extremely');

INSERT INTO choices (question_id, choice_text) VALUES
(59, 'Not at all'),
(59, 'Slightly'),
(59, 'Moderately'),
(59, 'Very'),
(59, 'Extremely');


DELETE FROM answers WHERE choice_id IS NULL AND question_id IN (
    SELECT id FROM questions WHERE question_type != 'TEXT'
);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES

(1, 2, 80, true),
(2, 2, 88, true),
(3, 2, NULL, false),
(4, 2, 95, true),
(5, 2, 1, true),
(5, 2, 3, true),


(1, 3, 81, true),
(2, 3, 89, false),
(4, 3, 96, true),
(5, 3, 2, true),
(5, 3, 61, true),


(1, 4, 79, false),
(2, 4, 85, true),
(3, 4, NULL, true),
(4, 4, 94, false),
(5, 4, 4, true),


(1, 5, 78, true),
(2, 5, 83, false),
(4, 5, 93, true),
(5, 5, 5, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES

(6, 2, 98, false),
(7, 2, 103, true),
(8, 2, NULL, false),
(9, 2, 107, true),
(10, 2, 8, true),
(10, 2, 9, true),


(6, 4, 99, true),
(7, 4, 104, false),
(8, 4, NULL, true),
(9, 4, 108, true),
(10, 4, 7, true),
(10, 4, 62, true),


(6, 9, 97, true),
(7, 9, 101, false),
(8, 9, NULL, true),
(9, 9, 106, true),
(10, 9, 6, false);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES

(11, 2, 112, true),
(12, 2, 115, false),
(13, 2, NULL, true),
(14, 2, 118, true),
(15, 2, 11, false),
(15, 2, 13, false),


(11, 5, 113, true),
(12, 5, 116, true),
(13, 5, NULL, false),
(14, 5, 119, true),
(15, 5, 12, true),
(15, 5, 63, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(11, 10, 111, true),
(12, 10, 117, false),
(14, 10, 117, true),
(15, 10, 14, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES

(16, 3, 123, true),
(17, 3, 126, false),
(18, 3, NULL, true),
(19, 3, 128, true),
(20, 3, 18, true),
(20, 3, 19, true),


(16, 9, 121, true),
(17, 9, 125, true),
(19, 9, 127, false),
(20, 9, 16, true),
(20, 9, 64, false);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES

(21, 3, 22, true),
(22, 4, 26, false),
(22, 4, 62, true),
(24, 2, 28, true),
(25, 5, 29, false),


(26, 2, 133, true),
(27, 3, 137, false),
(28, 4, NULL, true),
(29, 5, 141, false),
(30, 9, 32, true),
(30, 9, 65, true),


(31, 5, 144, true),
(32, 9, 149, false),
(33, 3, NULL, true),
(34, 4, 36, true),
(34, 4, 39, false),
(35, 10, 151, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(36, 9, 153, true),
(37, 10, 157, false),
(38, 2, NULL, true),
(39, 4, 161, true),
(40, 5, 41, true),
(40, 5, 43, false);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(41, 9, 164, true),
(42, 10, 167, false),
(43, 2, NULL, true),
(44, 3, 169, false),
(45, 5, 48, true),
(45, 5, 49, true);


INSERT INTO choices (question_id, choice_text) VALUES
(65, 'No'),
(65, 'Yes');


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(46, 2, 172, false),
(47, 2, 176, true),
(48, 2, NULL, false),
(49, 2, 179, true),
(50, 2, 51, true),
(50, 2, 53, false),

(46, 3, 173, true),
(47, 3, 177, false),
(48, 3, NULL, true),
(49, 3, 179, true),
(50, 3, 52, false),
(50, 3, 54, true),

(46, 9, 171, true),
(47, 9, 175, false),
(48, 9, NULL, false),
(49, 9, 177, true),
(50, 9, 55, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(51, 3, 52, true),
(51, 4, 51, false),
(51, 5, 53, true),
(51, 9, 54, true),
(51, 10, 51, false),

(52, 3, 55, true),
(52, 4, 56, false),
(52, 5, 57, true),
(52, 9, 58, false),
(52, 10, 59, true),

(53, 3, NULL, true),
(53, 4, NULL, false),
(53, 5, NULL, true),

(54, 3, 174, false),
(54, 4, 175, true),
(54, 5, 171, false),
(54, 9, 172, true),
(54, 10, 175, false),

(55, 3, 60, false),
(55, 4, 61, true),
(55, 5, 62, true),
(55, 9, 60, false),
(55, 10, 63, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(56, 2, 66, true),
(56, 4, 67, false),
(56, 5, 68, true),
(56, 9, 69, false),
(56, 10, 70, true),

(57, 2, 71, true),
(57, 2, 72, true),
(57, 4, 71, false),
(57, 4, 73, true),
(57, 5, 71, false),
(57, 5, 74, true),
(57, 9, 71, true),
(57, 9, 75, false),
(57, 10, 71, true),
(57, 10, 72, true),
(57, 10, 73, true),

(58, 2, NULL, false),
(58, 4, NULL, true),
(58, 5, NULL, false),
(58, 9, NULL, false),

(59, 2, 174, true),
(59, 4, 175, false),
(59, 5, 176, true),
(59, 9, 172, false),
(59, 10, 171, true),

(60, 2, 76, false),
(60, 4, 77, true),
(60, 5, 78, false),
(60, 9, 79, true),
(60, 10, 80, false);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(16, 'How concerned are you about climate change?', 'SINGLE', 5),
(16, 'Which environmental issues are most important to you?', 'MULTIPLE', NULL),
(16, 'How do you reduce your environmental impact?', 'MULTIPLE', NULL),
(16, 'What environmental policies would you support?', 'TEXT', NULL),
(16, 'How often do you participate in environmental activities?', 'MULTIPLE', NULL),

(17, 'How would you rate your current fitness level?', 'SINGLE', 5),
(17, 'What are your primary fitness goals?', 'MULTIPLE', NULL),
(17, 'How often do you exercise?', 'MULTIPLE', NULL),
(17, 'What types of exercise do you prefer?', 'MULTIPLE', NULL),
(17, 'What health metrics do you track regularly?', 'MULTIPLE', NULL),

(18, 'How many hours per week do you spend gaming?', 'MULTIPLE', NULL),
(18, 'What gaming platforms do you use?', 'MULTIPLE', NULL),
(18, 'What genres of games do you prefer?', 'MULTIPLE', NULL),
(18, 'Do you participate in online gaming communities?', 'SINGLE', NULL),
(18, 'What features are most important in games you play?', 'MULTIPLE', NULL);


INSERT INTO choices (question_id, choice_text) VALUES
(61, 'Facebook'),
(61, 'Instagram'),
(61, 'Twitter/X'),
(61, 'LinkedIn'),
(61, 'TikTok'),
(61, 'YouTube'),
(61, 'Pinterest'),
(61, 'Reddit'),
(61, 'Snapchat'),
(61, 'Discord'),

(62, 'Less than 30 minutes'),
(62, '30 minutes to 1 hour'),
(62, '1-2 hours'),
(62, '2-4 hours'),
(62, 'More than 4 hours'),
(62, '4-6 hours'),

(63, 'News and current events'),
(63, 'Entertainment and celebrity content'),
(63, 'Educational content'),
(63, 'Sports content'),
(63, 'Personal connections/friends updates'),
(63, 'Professional/career content'),
(63, 'Hobby-related content'),
(63, 'DIY/Crafting content'),
(63, 'Travel content');


INSERT INTO choices (question_id, choice_text) VALUES
(66, 'Very Unsatisfied'),
(66, 'Unsatisfied'),
(66, 'Neutral'),
(66, 'Satisfied'),
(66, 'Very Satisfied'),

(67, 'Checking Account'),
(67, 'Savings Account'),
(67, 'Credit Card'),
(67, 'Mortgage/Loan'),
(67, 'Investment Account'),
(67, 'Retirement Fund'),
(67, 'Insurance Products'),
(67, 'Mobile Banking'),
(67, 'Financial Advisory Services'),

(69, 'Not Important'),
(69, 'Slightly Important'),
(69, 'Moderately Important'),
(69, 'Very Important'),
(69, 'Extremely Important'),

(70, 'Weekly'),
(70, 'Monthly'),
(70, 'Every few months'),
(70, 'Once or twice a year'),
(70, 'Never (digital banking only)');


INSERT INTO choices (question_id, choice_text) VALUES
(71, 'Very Unproductive'),
(71, 'Somewhat Unproductive'),
(71, 'Same as Office'),
(71, 'Somewhat More Productive'),
(71, 'Much More Productive'),

(72, 'Distractions at home'),
(72, 'Isolation/Loneliness'),
(72, 'Communication difficulties'),
(72, 'Technical issues'),
(72, 'Work-life boundary blurring'),
(72, 'Lack of proper workspace'),
(72, 'Reduced team collaboration'),
(72, 'Family interruptions'),
(72, 'Inadequate equipment'),

(74, 'Fully remote'),
(74, 'Mostly remote with occasional office'),
(74, 'Hybrid (equal split)'),
(74, 'Mostly office with occasional remote'),
(74, 'Fully in-office'),
(74, 'Flexible arrangement'),

(75, '0 days (fully in-office)'),
(75, '1 day per week'),
(75, '2 days per week'),
(75, '3 days per week'),
(75, '4 days per week'),
(75, '5 days per week (fully remote)');


INSERT INTO choices (question_id, choice_text) VALUES
(76, 'Not at all concerned'),
(76, 'Slightly concerned'),
(76, 'Moderately concerned'),
(76, 'Very concerned'),
(76, 'Extremely concerned'),

(77, 'Climate change'),
(77, 'Plastic pollution'),
(77, 'Deforestation'),
(77, 'Air quality'),
(77, 'Water conservation'),
(77, 'Endangered species'),
(77, 'Ocean acidification'),
(77, 'Waste management'),

(78, 'Recycling'),
(78, 'Using reusable bags/bottles'),
(78, 'Public transportation'),
(78, 'Energy conservation'),
(78, 'Reducing meat consumption'),
(78, 'Composting'),
(78, 'Buying local products'),
(78, 'Using renewable energy'),

(80, 'Never'),
(80, 'Rarely'),
(80, 'Sometimes'),
(80, 'Often'),
(80, 'Regularly');


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(61, 2, 201, true),
(61, 2, 202, true),
(61, 3, 203, false),
(61, 3, 207, true),
(61, 4, 202, true),
(61, 4, 204, false),
(61, 5, 205, true),
(61, 5, 206, true),
(61, 9, 208, false),
(61, 9, 210, true),
(61, 10, 206, false),
(61, 10, 209, true),

(62, 2, 211, true),
(62, 3, 212, false),
(62, 4, 213, true),
(62, 5, 214, false),
(62, 9, 215, true),
(62, 10, 216, false),
 
(63, 2, 217, true),
(63, 2, 223, true),
(63, 3, 218, false),
(63, 3, 225, true),
(63, 4, 219, true),
(63, 5, 220, false),
(63, 9, 221, true),
(63, 10, 224, false),
 
(64, 2, NULL, false),
(64, 3, NULL, true),
(64, 4, NULL, false),
(64, 5, NULL, true),
 
(65, 2, 183, true),
(65, 3, 182, false),
(65, 4, 183, true),
(65, 5, 182, false),
(65, 9, 183, true),
(65, 10, 182, false);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(66, 2, 230, true),
(66, 3, 227, false),
(66, 4, 231, true),
(66, 5, 229, false),
(66, 9, 228, true),
(66, 10, 227, false),
 
(67, 2, 232, true),
(67, 2, 233, true),
(67, 3, 234, false),
(67, 3, 236, true),
(67, 4, 233, false),
(67, 4, 237, true),
(67, 5, 235, true),
(67, 9, 239, false),
(67, 10, 240, true),
 
(68, 2, NULL, false),
(68, 3, NULL, true),
(68, 4, NULL, false),
(68, 5, NULL, true),
 
(69, 2, 241, true),
(69, 3, 243, false),
(69, 4, 244, true),
(69, 5, 245, false),
(69, 9, 242, true),
(69, 10, 244, false),
 
(70, 2, 247, true),
(70, 3, 246, false),
(70, 4, 248, true),
(70, 5, 249, false),
(70, 9, 250, true),
(70, 10, 248, false);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(71, 2, 254, true),
(71, 3, 251, false),
(71, 4, 255, true),
(71, 5, 253, false),
(71, 9, 252, true),
 
(72, 2, 256, true),
(72, 2, 260, true),
(72, 3, 257, false),
(72, 3, 259, true),
(72, 4, 261, false),
(72, 5, 262, true),
(72, 9, 263, false),
(72, 10, 264, true),
 
(73, 2, NULL, false),
(73, 3, NULL, true),
(73, 4, NULL, false),
(73, 5, NULL, true),
 
(74, 2, 265, true),
(74, 3, 268, false),
(74, 4, 266, true),
(74, 5, 267, false),
(74, 9, 269, true),
(74, 10, 270, false),
 
(75, 2, 276, true),
(75, 3, 271, false),
(75, 4, 275, true),
(75, 5, 274, false),
(75, 9, 272, true),
(75, 10, 273, false);


INSERT INTO choices (question_id, choice_text) VALUES
(81, 'Poor'),
(81, 'Below Average'),
(81, 'Average'),
(81, 'Good'),
(81, 'Excellent'),

(82, 'Weight loss'),
(82, 'Muscle building'),
(82, 'Overall health improvement'),
(82, 'Stress reduction'),
(82, 'Athletic performance'),
(82, 'Flexibility'),
(82, 'Endurance'),

(83, 'Never'),
(83, '1-2 times per week'),
(83, '3-4 times per week'),
(83, '5-6 times per week'),
(83, 'Daily'),

(84, 'Cardio/Running'),
(84, 'Weight training'),
(84, 'Yoga/Pilates'),
(84, 'Swimming'),
(84, 'Team sports'),
(84, 'Cycling'),
(84, 'HIIT workouts'),
(84, 'Walking'),

(85, 'Weight'),
(85, 'Heart rate'),
(85, 'Steps'),
(85, 'Sleep quality'),
(85, 'Calories burned'),
(85, 'Blood pressure'),
(85, 'Workout performance');


INSERT INTO choices (question_id, choice_text) VALUES
(86, 'Less than 1 hour'),
(86, '1-5 hours'),
(86, '6-10 hours'),
(86, '11-20 hours'),
(86, 'More than 20 hours'),

(87, 'PC'),
(87, 'PlayStation'),
(87, 'Xbox'),
(87, 'Nintendo Switch'),
(87, 'Mobile devices'),
(87, 'VR headsets'),

(88, 'Action/Adventure'),
(88, 'RPG'),
(88, 'FPS/Shooter'),
(88, 'Strategy'),
(88, 'Simulation'),
(88, 'Sports'),
(88, 'Puzzle'),
(88, 'MMORPG'),
(88, 'Fighting'),

(89, 'No'),
(89, 'Occasionally'),
(89, 'Regularly'),
(89, 'Very active'),

(90, 'Graphics quality'),
(90, 'Story/Narrative'),
(90, 'Gameplay mechanics'),
(90, 'Multiplayer capabilities'),
(90, 'Customization options'),
(90, 'Replayability'),
(90, 'Open world');


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(76, 2, 279, true),
(76, 3, 277, false),
(76, 4, 280, true),
(76, 5, 278, false),
(76, 9, 276, true),
(76, 10, 279, false),

(77, 2, 281, true),
(77, 2, 282, true),
(77, 3, 283, false),
(77, 3, 284, true),
(77, 4, 285, false),
(77, 4, 286, true),
(77, 5, 287, false),
(77, 5, 288, true),
(77, 9, 282, true),
(77, 10, 281, false),

(78, 2, 289, true),
(78, 2, 290, true),
(78, 3, 291, false),
(78, 3, 292, true),
(78, 4, 293, false),
(78, 4, 294, true),
(78, 5, 295, false),
(78, 5, 296, true),
(78, 9, 289, false),
(78, 10, 292, true),

(79, 2, NULL, false),
(79, 3, NULL, true),
(79, 4, NULL, false),
(79, 5, NULL, true),

(80, 2, 298, true),
(80, 3, 299, false),
(80, 4, 300, true),
(80, 5, 301, false),
(80, 9, 297, true),
(80, 10, 299, false);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(81, 2, 303, true),
(81, 3, 302, false),
(81, 4, 304, true),
(81, 5, 305, false),
(81, 9, 301, true),
(81, 10, 303, false),

(82, 2, 306, true),
(82, 2, 308, true),
(82, 3, 307, false),
(82, 3, 309, true),
(82, 4, 310, false),
(82, 4, 311, true),
(82, 5, 312, false),
(82, 9, 308, true),
(82, 10, 309, false),

(83, 2, 314, true),
(83, 3, 315, false),
(83, 4, 316, true),
(83, 5, 317, false),
(83, 9, 313, true),
(83, 10, 315, false),

(84, 2, 318, true),
(84, 2, 325, true),
(84, 3, 319, false),
(84, 3, 320, true),
(84, 4, 321, false),
(84, 4, 322, true),
(84, 5, 323, false),
(84, 5, 324, true),
(84, 9, 325, false),
(84, 10, 320, true),

(85, 2, 326, true),
(85, 2, 328, true),
(85, 3, 327, false),
(85, 3, 329, true),
(85, 4, 330, false),
(85, 5, 331, true),
(85, 9, 332, false),
(85, 10, 328, true);


INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(86, 2, 333, true),
(86, 3, 334, false),
(86, 4, 335, true),
(86, 5, 336, false),
(86, 9, 334, true),
(86, 10, 337, false),

(87, 2, 338, true),
(87, 3, 339, false),
(87, 4, 340, true),
(87, 5, 341, false),
(87, 9, 342, true),
(87, 10, 343, false),
(87, 10, 338, true),

(88, 2, 344, true),
(88, 3, 345, false),
(88, 4, 346, true),
(88, 5, 347, false),
(88, 9, 348, true),
(88, 10, 349, false),
(88, 2, 350, true),
(88, 3, 351, false),
(88, 4, 352, true),

(89, 2, 353, true),
(89, 3, 354, false),
(89, 4, 355, true),
(89, 5, 356, false),
(89, 9, 354, true),
(89, 10, 356, false),

(90, 2, 357, true),
(90, 2, 358, true),
(90, 3, 359, false),
(90, 3, 360, true),
(90, 4, 361, false),
(90, 5, 362, true),
(90, 9, 363, false),
(90, 10, 358, true);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(19, 'How many books do you read in a year?', 'MULTIPLE', NULL),
(19, 'What genres do you prefer to read?', 'MULTIPLE', NULL),
(19, 'How do you prefer to consume books?', 'MULTIPLE', NULL),
(19, 'What influences your book selection?', 'MULTIPLE', NULL),
(19, 'What was the last book you really enjoyed?', 'TEXT', NULL);

INSERT INTO choices (question_id, choice_text) VALUES
(91, '0-1 books'),
(91, '2-5 books'),
(91, '6-12 books'),
(91, '13-24 books'),
(91, '25+ books'),

(92, 'Fiction - Contemporary'),
(92, 'Fiction - Classics'),
(92, 'Science Fiction'),
(92, 'Fantasy'),
(92, 'Mystery/Thriller'),
(92, 'Romance'),
(92, 'Non-fiction - History'),
(92, 'Non-fiction - Science'),
(92, 'Biography/Memoir'),
(92, 'Self-help/Personal development'),

(93, 'Physical books'),
(93, 'E-books'),
(93, 'Audiobooks'),
(93, 'Library loans'),
(93, 'Subscription services'),

(94, 'Friend recommendations'),
(94, 'Online reviews'),
(94, 'Social media'),
(94, 'Bestseller lists'),
(94, 'Book clubs'),
(94, 'Subject interest'),
(94, 'Author familiarity');

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(91, 2, 364, true),
(91, 3, 365, false),
(91, 4, 366, true),
(91, 5, 367, false),
(91, 9, 368, true),

(92, 2, 369, true),
(92, 3, 370, false),
(92, 4, 371, true),
(92, 5, 372, false),
(92, 9, 373, true),
(92, 10, 374, false),
(92, 2, 375, true),
(92, 3, 378, false),

(93, 2, 379, true),
(93, 3, 380, false),
(93, 4, 381, true),
(93, 5, 382, false),
(93, 9, 383, true),
(93, 10, 380, false),

(94, 2, 384, true),
(94, 3, 385, false),
(94, 4, 386, true),
(94, 5, 387, false),
(94, 9, 388, true),
(94, 10, 389, false),
(94, 2, 390, true);


INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(20, 'How often do you shop online?', 'MULTIPLE', NULL),
(20, 'What factors influence your purchase decisions?', 'MULTIPLE', NULL),
(20, 'What payment methods do you prefer?', 'MULTIPLE', NULL),
(20, 'How important is sustainability in your purchase decisions?', 'SINGLE', 5),
(20, 'What improvements would you like to see in online shopping?', 'TEXT', NULL);

INSERT INTO choices (question_id, choice_text) VALUES
(95, 'Daily'),
(95, 'Weekly'),
(95, 'Monthly'),
(95, 'Few times a year'),
(95, 'Rarely'),

(96, 'Price'),
(96, 'Product quality'),
(96, 'Brand reputation'),
(96, 'Customer reviews'),
(96, 'Free shipping'),
(96, 'Return policy'),
(96, 'Product availability'),
(96, 'Discounts/sales'),

(97, 'Credit card'),
(97, 'Debit card'),
(97, 'PayPal/digital wallets'),
(97, 'Buy now, pay later'),
(97, 'Bank transfer'),

(98, 'Not important'),
(98, 'Slightly important'),
(98, 'Moderately important'),
(98, 'Very important'),
(98, 'Extremely important');

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(95, 2, 391, true),
(95, 3, 392, false),
(95, 4, 393, true),
(95, 5, 394, false),
(95, 9, 395, true),
(95, 10, 392, false),

(96, 2, 396, true),
(96, 2, 399, true),
(96, 3, 397, false),
(96, 3, 400, true),
(96, 4, 398, false),
(96, 4, 401, true),
(96, 5, 402, false),
(96, 5, 403, true),
(96, 9, 396, false),
(96, 10, 397, true),

(97, 2, 404, true),
(97, 3, 405, false),
(97, 4, 406, true),
(97, 5, 407, false),
(97, 9, 408, true),
(97, 10, 404, false),

(98, 2, 409, true),
(98, 3, 410, false),
(98, 4, 411, true),
(98, 5, 412, false),
(98, 9, 413, true),
(98, 10, 411, false);